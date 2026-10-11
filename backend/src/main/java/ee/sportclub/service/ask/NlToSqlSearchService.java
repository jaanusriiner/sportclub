package ee.sportclub.service.ask;

import ee.sportclub.controller.ask.dto.BenchmarkAskRequest;
import ee.sportclub.controller.ask.dto.BenchmarkAskResponse;
import ee.sportclub.infrastructure.llm.LlmChatClients;
import ee.sportclub.infrastructure.llm.LlmChatOptionsFactory;
import ee.sportclub.infrastructure.llm.LlmCost;
import ee.sportclub.infrastructure.llm.LlmCostCalculator;
import ee.sportclub.infrastructure.llm.LlmStructuredOutput;
import ee.sportclub.infrastructure.llm.LlmTokenUsage;
import ee.sportclub.infrastructure.llm.MetricsAdvisor;
import ee.sportclub.infrastructure.util.CsvFormatter;
import ee.sportclub.persistence.llmmodel.LlmModel;
import ee.sportclub.persistence.tone.Tone;
import ee.sportclub.service.ask.SqlConversationStore.SqlConversation;
import ee.sportclub.service.ask.SqlConversationStore.SqlQueryResult;
import ee.sportclub.service.llmmodel.LlmModelService;
import ee.sportclub.service.tone.ToneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Benchmarki SQL-režiim (/api/ask3): täisandmestiku asemel saab LLM alguses ainult väikese kataloogi
// (spordialad, piirkonnad, asukohad) ja koostab ise SELECT-i, mis toob küsimuseks vajalikud treeningud.
//
// Üks küsimus = kaks LLM kutset:
//   1. samm (sql-decision): kas on vaja uut päringut? kui jah, siis milline SELECT (LlmSqlDecision)
//   2. samm (answer): vastus kataloogi ja vestluse jooksul toodud päringutulemuste põhjal (LlmAnswer)
// Vestluse olek (kataloog + päringute tulemused) on SqlConversationStore'is, nii et jätkuküsimusel võib LLM
// vastata ka juba toodud andmetest ilma uue päringuta.
@Slf4j
@Service
@RequiredArgsConstructor
public class NlToSqlSearchService {

    private static final String STEP_SQL_DECISION = "sql-decision";
    private static final String STEP_ANSWER = "answer";

    private static final String VIEW_COLUMNS = """
            v_training_date_extended(
              training_group_name TEXT, training_group_description TEXT,
              sport_name TEXT, skill_level_name TEXT,
              facility_name TEXT, facility_address TEXT, facility_description TEXT, area_name TEXT,
              trainer_name TEXT, sportclub_name TEXT,
              training_date DATE, training_time TIME, training_date_duration INTEGER (minutes),
              training_date_max_size INTEGER (max participants), training_weekdays TEXT (E,T,K,N,R,L,P = Mon..Sun),
              review_description TEXT
            )""";

    private static final String SQL_DECISION_SYSTEM_PROMPT_TEMPLATE = """
            You are the query planner of a search assistant for customers visiting a Sportclub webpage.
            Customers ask about trainings: sports, skill levels, locations, areas, clubs, trainers, dates and times.
            Questions might be in Estonian; data values (sport, level, area, facility names) are in Estonian.

            You do not see training data directly. To get it, you may write ONE PostgreSQL SELECT statement against:
            %s
            The view contains only active trainings.

            In the "reasoning" field, briefly explain your decision: whether the question is about sport and
            training options, whether CATALOG or PREVIOUS QUERIES already answer it, and if not, which data the query
            fetches.

            DECISION:
            - Return "sql": null if the question is unrelated to sports, or can be answered from CATALOG or from the
              results of PREVIOUS QUERIES.
            - Otherwise return one SELECT statement in "sql".

            SQL RULES:
            1. Only SELECT from v_training_date_extended. Never change data. No other tables, no comments.
            2. Filter training_date >= CURRENT_DATE unless the customer explicitly asks about the past.
            3. Select only the columns needed to answer. Use exact values from CATALOG for sport_name,
               skill_level_name, area_name and facility_name (ILIKE is fine for partial names).
            4. area_name is the area of the facility: a city (e.g. Tallinn) or a county (e.g. Harjumaa). When the
               customer asks about a county, also include the cities located in that county.
            5. Results are limited to %d rows: add ORDER BY training_date, training_time and filters that keep the
               result small.

            CATALOG:
            %s""";

    private static final String SQL_DECISION_USER_PROMPT_TEMPLATE = """
            Decide whether a new query is needed and write it if so.

            Question: %s""";

    private static final String SQL_FIX_USER_PROMPT_TEMPLATE = """
            Your previous SQL could not be executed:
            %s
            Error: %s
            Return a corrected SQL statement (or null if no query is needed).

            Question: %s""";

    private static final String ANSWER_SYSTEM_PROMPT_TEMPLATE = """
            You are search engine for customers visiting Sportclub webpage.
            Answer the customer's question using QUERY RESULTS (trainings found for this conversation) and CATALOG
            (sports, areas and facilities). Question might be in Estonian, answer in the language of the question.

            In the "reasoning" field, briefly explain which data the answer is based on (which QUERY RESULTS rows or
            CATALOG entries), and whether a clarifying question to the customer is needed instead.

            RULES:
            1. Use only facts from QUERY RESULTS and CATALOG. If they do not contain the answer, say so honestly.
            2. Each query result is limited to %d rows; if a result has exactly %d rows, mention there may be more.
            3. If the question is not about sports or trainings, return: Põnev küsimus :) , aga kahjuks minu teadmiste väline

            CATALOG:
            %s""";

    private static final String ANSWER_USER_PROMPT_TEMPLATE = """
            Answer this question in 1-3 plain sentences using the QUERY RESULTS and CATALOG in the system prompt.
            Don't mention SQL or technical terms. Be specific about numbers.

            Question: %s""";

    private static final String SPORTS_SQL = """
            SELECT s.name AS sport, string_agg(sl.name, ', ' ORDER BY sl.id) AS levels
            FROM sport s LEFT JOIN skill_level sl ON sl.sport_id = s.id
            GROUP BY s.id, s.name
            ORDER BY s.name""";

    private static final String AREAS_SQL = "SELECT name AS area FROM area ORDER BY name";

    private static final String FACILITIES_SQL = """
            SELECT f.name AS facility, a.name AS area, string_agg(s.name, ', ' ORDER BY s.name) AS sports
            FROM facility f
                     JOIN area a ON a.id = f.area_id
                     LEFT JOIN sport_facility sf ON sf.facility_id = f.id
                     LEFT JOIN sport s ON s.id = sf.sport_id
            GROUP BY f.id, f.name, a.name
            ORDER BY f.name""";

    private final JdbcTemplate jdbcTemplate;
    private final LlmChatClients llmChatClients;
    private final LlmChatOptionsFactory llmChatOptionsFactory;
    private final LlmCostCalculator llmCostCalculator;
    private final LlmModelService llmModelService;
    private final ToneService toneService;
    private final SqlConversationStore sqlConversationStore;
    private final BenchmarkConversationMemory benchmarkConversationMemory;
    private final SqlQueryValidator sqlQueryValidator;
    private final SqlQueryExecutor sqlQueryExecutor;

    public BenchmarkAskResponse ask(BenchmarkAskRequest request) {
        LlmModel llmModel = llmModelService.getValidLlmModelBy(request.llmModelId());
        Tone tone = toneService.getValidToneBy(request.toneId());
        SqlConversation sqlConversation = getSqlConversation(request);

        // 1. samm: kas ja milline päring (vea korral üks parandus-katse)
        LlmCallResult<LlmSqlDecision> sqlDecision = decideSqlQuery(request, llmModel, sqlConversation, null);
        LlmTokenUsage tokenUsage = sqlDecision.tokenUsage();
        SqlQueryResult sqlQueryResult = null;
        String sql = sqlDecision.entity().sql();
        if (hasSql(sql)) {
            try {
                sqlQueryResult = runSqlQuery(sql);
            } catch (IllegalArgumentException | DataAccessException exception) {
                log.info("LLM-i SQL ei läinud läbi, palume parandust: {}", exception.getMessage());
                LlmCallResult<LlmSqlDecision> fixedSqlDecision = decideSqlQuery(request, llmModel, sqlConversation,
                        new FailedSql(sql, exception.getMessage()));
                tokenUsage = tokenUsage.plus(fixedSqlDecision.tokenUsage());
                sql = fixedSqlDecision.entity().sql();
                sqlQueryResult = hasSql(sql) ? runSqlQuery(sql) : null;  // teine viga läheb kasutajale veana
            }
        }
        if (sqlQueryResult != null) {
            sqlConversation.queryResults().add(sqlQueryResult);
        }

        // 2. samm: vastus kataloogi ja kõigi vestluse päringutulemuste põhjal
        LlmCallResult<LlmAnswer> answer = generateAnswer(request, llmModel, tone, sqlConversation);
        tokenUsage = tokenUsage.plus(answer.tokenUsage());
        benchmarkConversationMemory.saveExchange(request.conversationId(), request.question(), answer.entity().answer());

        return createResponse(answer.entity(), tokenUsage, llmModel, sqlQueryResult);
    }

    // uus vestlus (serveris pole selle id-ga ajalugu) või mälust kadunud olek (nt restart) -> loeme kataloogi uuesti
    private SqlConversation getSqlConversation(BenchmarkAskRequest request) {
        boolean isFirstQuestion = benchmarkConversationMemory.isNewConversation(request.conversationId());
        if (!isFirstQuestion) {
            var savedConversation = sqlConversationStore.findConversationBy(request.conversationId());
            if (savedConversation.isPresent()) {
                return savedConversation.get();
            }
        }
        return sqlConversationStore.saveNewConversation(request.conversationId(), getCatalog());
    }

    private String getCatalog() {
        List<Map<String, Object>> sports = jdbcTemplate.queryForList(SPORTS_SQL);
        List<Map<String, Object>> areas = jdbcTemplate.queryForList(AREAS_SQL);
        List<Map<String, Object>> facilities = jdbcTemplate.queryForList(FACILITIES_SQL);
        return "SPORTS (" + sports.size() + " rows):\n" + CsvFormatter.toCsv(sports)
                + "AREAS (" + areas.size() + " rows):\n" + CsvFormatter.toCsv(areas)
                + "FACILITIES (" + facilities.size() + " rows):\n" + CsvFormatter.toCsv(facilities);
    }

    private LlmCallResult<LlmSqlDecision> decideSqlQuery(BenchmarkAskRequest request, LlmModel llmModel,
                                                         SqlConversation sqlConversation, FailedSql failedSql) {
        // system prompt: muutumatu osa (reeglid + kataloog) ees, et provider saaks seda vahemälust lugeda;
        // vestluse jooksul lisanduvad päringud lõpus
        String systemPrompt = SQL_DECISION_SYSTEM_PROMPT_TEMPLATE.formatted(
                VIEW_COLUMNS, SqlQueryExecutor.MAX_ROWS, sqlConversation.catalog())
                + getPreviousQueriesSummary(sqlConversation);
        String userPrompt = failedSql == null
                ? SQL_DECISION_USER_PROMPT_TEMPLATE.formatted(request.question())
                : SQL_FIX_USER_PROMPT_TEMPLATE.formatted(failedSql.sql(), failedSql.error(), request.question());
        LlmCallResult<LlmSqlDecision> sqlDecision = callLlm(request, llmModel, systemPrompt, userPrompt,
                LlmSqlDecision.class, STEP_SQL_DECISION);
        // iga LLM-i genereeritud SQL logisse - ka see, mille validaator hiljem tagasi lükkab
        log.info("[SqlMode] generated_sql conversationId={} model={} attempt={} sql={}",
                request.conversationId(), llmModel.getName(), failedSql == null ? "first" : "fix",
                toSingleLine(sqlDecision.entity().sql()));
        return sqlDecision;
    }

    // 1. sammule ainult päringud ja ridade arv (mitte read endid) - otsuseks piisab, input jääb väikeseks
    private String getPreviousQueriesSummary(SqlConversation sqlConversation) {
        List<SqlQueryResult> queryResults = List.copyOf(sqlConversation.queryResults());
        if (queryResults.isEmpty()) {
            return "\nPREVIOUS QUERIES: none\n";
        }
        StringBuilder summary = new StringBuilder("\nPREVIOUS QUERIES (results are available to the answer step):\n");
        for (int index = 0; index < queryResults.size(); index++) {
            SqlQueryResult queryResult = queryResults.get(index);
            summary.append(index + 1).append(". ").append(queryResult.sql())
                    .append(" -> ").append(queryResult.rowCount()).append(" rows\n");
        }
        return summary.toString();
    }

    private SqlQueryResult runSqlQuery(String sql) {
        String validatedSql = sqlQueryValidator.getValidatedSql(sql);
        long startNanos = System.nanoTime();
        List<Map<String, Object>> rows = sqlQueryExecutor.findRowsBy(validatedSql);
        log.info("[SqlMode] executed_sql rows={} durationMs={} sql={}", rows.size(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos), toSingleLine(validatedSql));
        return new SqlQueryResult(validatedSql, rows.size(), CsvFormatter.toCsv(rows));
    }

    // mitmerealine SQL ühele logireale (null = LLM otsustas päringut mitte teha)
    private String toSingleLine(String sql) {
        return sql == null ? null : sql.strip().replaceAll("\\s+", " ");
    }

    private LlmCallResult<LlmAnswer> generateAnswer(BenchmarkAskRequest request, LlmModel llmModel, Tone tone,
                                                    SqlConversation sqlConversation) {
        String systemPrompt = ANSWER_SYSTEM_PROMPT_TEMPLATE.formatted(
                SqlQueryExecutor.MAX_ROWS, SqlQueryExecutor.MAX_ROWS, sqlConversation.catalog())
                + getQueryResults(sqlConversation)
                + getToneInstruction(tone);
        String userPrompt = ANSWER_USER_PROMPT_TEMPLATE.formatted(request.question());
        return callLlm(request, llmModel, systemPrompt, userPrompt, LlmAnswer.class, STEP_ANSWER);
    }

    private String getQueryResults(SqlConversation sqlConversation) {
        List<SqlQueryResult> queryResults = List.copyOf(sqlConversation.queryResults());
        if (queryResults.isEmpty()) {
            return "\nQUERY RESULTS: none\n";
        }
        StringBuilder results = new StringBuilder("\nQUERY RESULTS:\n");
        for (int index = 0; index < queryResults.size(); index++) {
            SqlQueryResult queryResult = queryResults.get(index);
            results.append("Result ").append(index + 1).append(" (").append(queryResult.rowCount()).append(" rows):\n")
                    .append(queryResult.rowsCsv());
        }
        return results.toString();
    }

    // tühja kirjelduse (NEUTRAL) korral tooni ei lisata
    private String getToneInstruction(Tone tone) {
        String toneDescription = tone.getDescription();
        if (toneDescription == null || toneDescription.isBlank()) {
            return "";
        }
        return "\nTONE:\n" + toneDescription;
    }

    private <T> LlmCallResult<T> callLlm(BenchmarkAskRequest request, LlmModel llmModel, String systemPrompt,
                                         String userPrompt, Class<T> responseType, String step) {
        // reasoning mudelid (nt OpenAI GPT-6, Gemini 3.x) ei luba temperatuuri muuta - siis jätame selle saatmata
        Double temperature = llmModel.getSupportsTemperature()
                ? Math.min(request.temperature(), llmModel.getMaxTemperature().doubleValue())
                : null;
        ChatOptions.Builder<?> chatOptionsBuilder = llmChatOptionsFactory.createChatOptionsBuilder(
                llmModel, temperature, request.thinkingLevel());
        ChatClient chatClient = llmChatClients.getChatClientBy(llmModel.getProvider());

        // JSON-vormingu juhis system prompti lõppu; vastuse teisendame ise, vt LlmStructuredOutput
        BeanOutputConverter<T> outputConverter = new BeanOutputConverter<>(responseType);
        ChatResponse chatResponse = chatClient.prompt()
                .options(chatOptionsBuilder)
                .advisors(advisorSpec -> advisorSpec
                        .param(MetricsAdvisor.LLM_MODEL_ID, llmModel.getId())
                        .param(MetricsAdvisor.CONVERSATION_ID, request.conversationId())
                        .param(MetricsAdvisor.STEP, step))
                .system(systemPrompt + "\n\n" + outputConverter.getFormat())
                .messages(benchmarkConversationMemory.getHistoryMessages(request.conversationId()))
                .user(userPrompt)
                .call()
                .chatResponse();

        T entity = LlmStructuredOutput.toEntity(chatResponse, outputConverter, llmModel.getName() + " (" + step + ")");
        LlmTokenUsage tokenUsage = LlmTokenUsage.from(chatResponse.getMetadata().getUsage());
        return new LlmCallResult<>(entity, tokenUsage);
    }

    // tokenid ja kulu on mõlema (või parandus-katsega kolme) LLM kutse summa
    private BenchmarkAskResponse createResponse(LlmAnswer llmAnswer, LlmTokenUsage tokenUsage, LlmModel llmModel,
                                                SqlQueryResult sqlQueryResult) {
        LlmCost cost = llmCostCalculator.calculateCost(tokenUsage, llmModel);
        return BenchmarkAskResponse.builder()
                .reasoning(llmAnswer.reasoning())
                .answer(llmAnswer.answer())
                .inputTokens(tokenUsage.inputTokens())
                .cachedTokens(tokenUsage.cachedTokens())
                .outputTokens(tokenUsage.outputTokens())
                .thinkingTokens(tokenUsage.thinkingTokens())
                .inputCost(cost.inputCost())
                .outputCost(cost.outputCost())
                .generatedSql(sqlQueryResult == null ? null : sqlQueryResult.sql())
                .sqlRowCount(sqlQueryResult == null ? null : sqlQueryResult.rowCount())
                .build();
    }

    // LLM-i JSON-is võib "tühja" päringu asemel olla ka tühi string või sõna null
    private boolean hasSql(String sql) {
        return sql != null && !sql.isBlank() && !sql.strip().equalsIgnoreCase("null");
    }

    private record LlmCallResult<T>(T entity, LlmTokenUsage tokenUsage) {
    }

    private record FailedSql(String sql, String error) {
    }
}
