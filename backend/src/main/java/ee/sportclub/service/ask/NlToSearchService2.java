package ee.sportclub.service.ask;

import ee.sportclub.controller.ask.dto.AskHistoryEntry;
import ee.sportclub.controller.ask.dto.BenchmarkAskRequest;
import ee.sportclub.controller.ask.dto.BenchmarkAskResponse;
import ee.sportclub.infrastructure.llm.LlmChatClients;
import ee.sportclub.infrastructure.llm.LlmChatOptionsFactory;
import ee.sportclub.infrastructure.llm.LlmTokenUsage;
import ee.sportclub.persistence.llmmodel.LlmModel;
import ee.sportclub.persistence.tone.Tone;
import ee.sportclub.service.llmmodel.LlmModelService;
import ee.sportclub.service.tone.ToneService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NlToSearchService2 {

    private static final String SUMMARY_SYSTEM_PROMPT_TEMPLATE = """
            You are customer support agent for customers visiting Sportclub webpage.
            As input along with customer/user prompt, you will get database printout with all active trainings from current date forward in system according to schema below
            User question might be in Estonian, use both Estonian and English language to search data.
            
            Once user shows interest to join trainings, guide user first to Register to system as new user (or login as existing one) to complete the registration to traininggroup and training.
            You are not capable to register user yourself to training. User has to do it himself after logging into portal.
            
            Before classifying, reason step by step in the “reasoning” field: (1) Is the question related to sport and training options? (2) Can user question be answered from provided dataset and schema? (3) Can customer be even answered before asking additional clarifying questions.
            
            DATA FORMAT:
            DATA contains two semicolon-separated CSV tables (first line is the header, values with ';' are quoted).
            FACILITIES - every facility that has upcoming trainings:
              facility, address, area, description
            TRAININGS - every upcoming active training session, ordered by date and time:
              date (YYYY-MM-DD), time (HH:MM), duration_min, sport, level, club, trainer, facility, max_participants, reviews
            TRAININGS.facility refers to FACILITIES.facility.
            
            RULES:
            1. If you cannot answer from this schema, return: Põnev küsimus :) , aga kahjuks minu teadmiste väline
            """;

    private static final String DATASET_TEMPLATE = """
            
            DATA:
            FACILITIES (%d rows):
            %s
            TRAININGS (%d rows):
            %s""";

    private static final String SUMMARY_USER_PROMPT_TEMPLATE = """
            Answer this question in 1-3 plain sentences using the DATA in the system prompt.
            Don't mention SQL or technical terms. Be specific about numbers.
            
            Question: %s
            """;

    // andmestik kompaktselt: ainult vastamiseks vajalikud veerud (ilma id-de ja e-postideta), asukoha andmed
    // eraldi tabelis, et pikad kirjeldused ei korduks igas treeningureas; veerunimed on CSV päis
    private static final String FACILITIES_SQL = """
            SELECT DISTINCT facility_name AS facility, facility_address AS address, area_name AS area,
                            facility_description AS description
            FROM v_training_date_extended
            WHERE training_date >= CURRENT_DATE
            ORDER BY facility_name""";

    private static final String TRAININGS_SQL = """
            SELECT training_date AS date, to_char(training_time, 'HH24:MI') AS time,
                   training_date_duration AS duration_min, sport_name AS sport, skill_level_name AS level,
                   sportclub_name AS club, trainer_name AS trainer, facility_name AS facility,
                   training_date_max_size AS max_participants, review_description AS reviews
            FROM v_training_date_extended
            WHERE training_date >= CURRENT_DATE
            ORDER BY training_date, training_time, facility_name""";

    private static final String CSV_SEPARATOR = ";";

    private static final BigDecimal TOKENS_PER_PRICE_UNIT = BigDecimal.valueOf(1_000_000);
    private static final int COST_SCALE = 6;

    private final JdbcTemplate jdbcTemplate;
    private final LlmChatClients llmChatClients;
    private final LlmChatOptionsFactory llmChatOptionsFactory;
    private final LlmModelService llmModelService;
    private final ToneService toneService;
    private final ConversationDataStore conversationDataStore;
    private final BigDecimal usdToEurRate;

    public NlToSearchService2(JdbcTemplate jdbcTemplate, LlmChatClients llmChatClients,
                              LlmChatOptionsFactory llmChatOptionsFactory,
                              LlmModelService llmModelService, ToneService toneService,
                              ConversationDataStore conversationDataStore,
                              @Value("${llm.usd-to-eur-rate}") BigDecimal usdToEurRate) {
        this.jdbcTemplate = jdbcTemplate;
        this.llmChatClients = llmChatClients;
        this.llmChatOptionsFactory = llmChatOptionsFactory;
        this.llmModelService = llmModelService;
        this.toneService = toneService;
        this.conversationDataStore = conversationDataStore;
        this.usdToEurRate = usdToEurRate;
    }

    public BenchmarkAskResponse ask(BenchmarkAskRequest request) {
        LlmModel llmModel = llmModelService.getValidLlmModelBy(request.llmModelId());
        Tone tone = toneService.getValidToneBy(request.toneId());
        String dataset = getConversationDataset(request);
        return generateResponse(request, llmModel, tone, dataset);
    }

    // vestluse esimesel küsimusel (ajalugu tühi) päritakse andmestik andmebaasist ja jäetakse vestluse id järgi meelde;
    // jätkuküsimustel kasutatakse sama andmestikku (kui see on mälust kadunud, nt restardi järel, päritakse uuesti)
    private String getConversationDataset(BenchmarkAskRequest request) {
        boolean isFirstQuestion = request.history() == null || request.history().isEmpty();
        if (!isFirstQuestion) {
            Optional<String> savedDataset = conversationDataStore.findDatasetBy(request.conversationId());
            if (savedDataset.isPresent()) {
                return savedDataset.get();
            }
        }
        List<Map<String, Object>> facilities = jdbcTemplate.queryForList(FACILITIES_SQL);
        List<Map<String, Object>> trainings = jdbcTemplate.queryForList(TRAININGS_SQL);
        String dataset = DATASET_TEMPLATE.formatted(facilities.size(), toCsv(facilities), trainings.size(), toCsv(trainings));
        conversationDataStore.saveDataset(request.conversationId(), dataset);
        return dataset;
    }

    // CSV: päis veerunimedest, iga rida eraldi real; null -> tühi väli
    private String toCsv(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "";
        }
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(CSV_SEPARATOR, rows.getFirst().keySet())).append('\n');
        for (Map<String, Object> row : rows) {
            List<String> fields = new ArrayList<>();
            for (Object value : row.values()) {
                fields.add(toCsvField(value));
            }
            csv.append(String.join(CSV_SEPARATOR, fields)).append('\n');
        }
        return csv.toString();
    }

    // eraldaja, jutumärgi või reavahetusega väärtus pannakse jutumärkidesse (jutumärgid kahekordistatakse)
    private String toCsvField(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(CSV_SEPARATOR) || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private BenchmarkAskResponse generateResponse(BenchmarkAskRequest request, LlmModel llmModel, Tone tone,
                                                 String dataset) {
        String userPrompt = SUMMARY_USER_PROMPT_TEMPLATE.formatted(request.question());
        // reasoning mudelid (nt OpenAI GPT-6, Gemini 3.x) ei luba temperatuuri muuta - siis jätame selle saatmata
        Double temperature = llmModel.getSupportsTemperature()
                ? getAllowedTemperature(request.temperature(), llmModel)
                : null;
        ChatOptions.Builder<?> chatOptionsBuilder = llmChatOptionsFactory.createChatOptionsBuilder(
                llmModel, temperature, request.thinkingLevel());

        ResponseEntity<ChatResponse, LlmAnswer> responseEntity = callLlm(llmModel, chatOptionsBuilder,
                getSystemPrompt(dataset, tone), toHistoryMessages(request.history()), userPrompt);

        LlmTokenUsage tokenUsage = LlmTokenUsage.from(responseEntity.getResponse().getMetadata().getUsage());

        return BenchmarkAskResponse.builder()
                .answer(responseEntity.getEntity().answer())
                .reasoning(responseEntity.getEntity().reasoning())
                .inputTokens(tokenUsage.inputTokens())
                .cachedTokens(tokenUsage.cachedTokens())
                .outputTokens(tokenUsage.outputTokens())
                .thinkingTokens(tokenUsage.thinkingTokens())
                .inputCost(calculateInputCostEur(tokenUsage, llmModel))
                .outputCost(calculateCostEur(tokenUsage.outputTokens(), llmModel.getOutputPriceUsd()))
                .build();
    }

    // järjekord: reeglid + andmestik (vestluse jooksul muutumatu prefiks, vahemällu sobiv), seejärel toon,
    // mida võib vestluse jooksul vahetada; tühja kirjelduse (NEUTRAL) korral tooni ei lisata
    private String getSystemPrompt(String dataset, Tone tone) {
        String systemPrompt = SUMMARY_SYSTEM_PROMPT_TEMPLATE + dataset;
        String toneDescription = tone.getDescription();
        if (toneDescription == null || toneDescription.isBlank()) {
            return systemPrompt;
        }
        return systemPrompt + "\nTONE:\n" + toneDescription;
    }

    // mõni provider lubab väiksemat ülempiiri kui 2.0 (nt Anthropic 1.0)
    private Double getAllowedTemperature(Double temperature, LlmModel llmModel) {
        return Math.min(temperature, llmModel.getMaxTemperature().doubleValue());
    }

    // inputTokens sisaldab cached ja cache write tokeneid: cached soodushinnaga, cache write (Anthropic) kirjutamise
    // hinnaga, ülejäänud tavahinnaga
    private BigDecimal calculateInputCostEur(LlmTokenUsage tokenUsage, LlmModel llmModel) {
        if (tokenUsage.inputTokens() == null) {
            return null;
        }
        int cacheWriteTokens = tokenUsage.cacheWriteTokens();
        int uncachedTokens = tokenUsage.inputTokens() - tokenUsage.cachedTokens() - cacheWriteTokens;
        BigDecimal cacheWritePriceUsd = llmModel.getCacheWritePriceUsd() != null
                ? llmModel.getCacheWritePriceUsd()
                : llmModel.getInputPriceUsd();
        return calculateCostEur(uncachedTokens, llmModel.getInputPriceUsd())
                .add(calculateCostEur(tokenUsage.cachedTokens(), llmModel.getCachedInputPriceUsd()))
                .add(calculateCostEur(cacheWriteTokens, cacheWritePriceUsd));
    }

    // hind on USD / 1M tokenit -> kulu EUR = tokenid * hind / 1 000 000 * kurss
    private BigDecimal calculateCostEur(Integer tokens, BigDecimal priceUsdPerMillionTokens) {
        if (tokens == null) {
            return null;
        }
        return BigDecimal.valueOf(tokens)
                .multiply(priceUsdPerMillionTokens)
                .multiply(usdToEurRate)
                .divide(TOKENS_PER_PRICE_UNIT, COST_SCALE, RoundingMode.HALF_UP);
    }

    private List<Message> toHistoryMessages(List<AskHistoryEntry> history) {
        List<Message> messages = new ArrayList<>();
        if (history != null) {
            for (AskHistoryEntry entry : history) {
                messages.add(new UserMessage(entry.question()));
                messages.add(new AssistantMessage(entry.answer()));
            }
        }
        return messages;
    }

    private ResponseEntity<ChatResponse, LlmAnswer> callLlm(LlmModel llmModel, ChatOptions.Builder<?> chatOptionsBuilder,
                                                            String systemPrompt, List<Message> historyMessages,
                                                            String userPrompt) {
        ChatClient chatClient = llmChatClients.getChatClientBy(llmModel.getProvider());
        ResponseEntity<ChatResponse, LlmAnswer> responseEntity = chatClient.prompt()
                .options(chatOptionsBuilder)
                .system(systemPrompt)
                .messages(historyMessages)
                .user(userPrompt)
                .call()
                .responseEntity(LlmAnswer.class);

        if (responseEntity.getResponse() == null || responseEntity.getEntity() == null) {
            throw new IllegalStateException("AI model returned an empty response");
        }

        return responseEntity;
    }
}
