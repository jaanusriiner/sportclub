package ee.sportclub.service.ask;

import ee.sportclub.controller.ask.dto.AskResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NlToSqlService {

    private static final String SQL_SYSTEM_PROMPT_TEMPLATE = """
            You are a %s query generator for an sportclub infosystem database.
            
            SCHEMA:
            area(id SERIAL PK, name VARCHAR(255))
            facility(id SERIAL PK, area_id INT FK→area.id, name VARCHAR(255), address VARCHAR(255), description VARCHAR(255))
            join_application(id SERIAL PK, user_id INT FK→user.id, training_group_id INT FK→training_group.id, status VARCHAR(3))
            review(id SERIAL PK, description VARCHAR(500), training_date_id INT FK→training_date.id)
            role(id SERIAL PK, name VARCHAR(255))
            skill_level(id SERIAL PK, sport_id INT FK→sport.id, name VARCHAR(30))
            sport(id SERIAL PK, name VARCHAR(255))
            sport_facility(id SERIAL PK, sport_id INT FK→sport.id, facility_id INT FK→facility.id)
            sportclub(id SERIAL PK, name VARCHAR(100))
            sportclub_trainer(id SERIAL PK, sportclub_id INT FK→sportclub.id, user_id INT FK→user.id)
            trainer_application(id SERIAL PK, user_id INT FK→user.id, sportclub_id INT FK→sportclub.id, status VARCHAR(3))
            training(id SERIAL PK, training_group_id INT FK→training_group.id, default_facility_id INT FK→facility.id, name VARCHAR(255), maxsize INT, description VARCHAR(255), default_start_date DATE, default_end_date DATE, default_start_time TIME, default_end_time TIME, duration INT, weekdays VARCHAR(255))
            training_date(id SERIAL PK, training_id INT FK→training.id, facility_id INT FK→facility.id, start_date DATE, start_time TIME, duration INT, status VARCHAR(3), user_count INT, max_size INT, date_added DATE)
            training_group(id SERIAL PK, sportclub_id INT FK→sportclub.id, sport_id INT FK→sport.id, user_id INT FK→user.id, name VARCHAR(100), description VARCHAR(255), skill_level_id INT FK→skill_level.id)
            "user"(id SERIAL PK, role_id INT FK→role.id, email VARCHAR(255), status VARCHAR(3))
            user_sport(id SERIAL PK, sport_id INT FK→sport.id, user_id INT FK→user.id)
            user_training(id SERIAL PK, user_id INT FK→user.id, training_date_id INT FK→training_date.id)
            user_training_group(id SERIAL PK, user_id INT FK→user.id, training_group_id INT FK→training_group.id)
            profile(id SERIAL PK, user_id INT FK→user.id, first_name VARCHAR(255), last_name VARCHAR(255), phone_number INT, area_id INT FK→area.id)
            
            RULES:
            1. Return ONLY a single raw SQL SELECT statement - no markdown or explanation.
            2. If you cannot answer from this schema, return: CANNOT_ANSWER
            3. Never change or delete data, meaning never generate e.g INSERT, UPDATE, DELETE, DROP or any non-SELECT statement.
            4. Use only %s SQL syntax and functions.
            """;

    private static final String SUMMARY_SYSTEM_PROMPT = """
            You are a helpful assistant that summarizes data clearly.""";

    private static final String SUMMARY_USER_PROMPT_TEMPLATE = """
            Summarize this database query result in 1-3 plain sentences.
            Don't mention SQL or technical terms. Be specific about numbers.
            
            Question: %s
            Results (%d rows): %s
            """;

    private static final String SQL_USER_PROMPT_TEMPLATE = """
            Generate SQL syntax for the question below
            
            %s
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ChatClient chatClient;
    private final String sqlSystemPrompt;

    public NlToSqlService(JdbcTemplate jdbcTemplate, ChatClient.Builder builder,
                          @Value("${nlsql.dialect}") String dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatClient = builder.build();
        this.sqlSystemPrompt = SQL_SYSTEM_PROMPT_TEMPLATE.formatted(dialect, dialect);
    }

    public AskResponse ask(String userQuestion) {
        String generatedSql = generateSql(userQuestion);
//        String generatedSql = "SELECT SUM(id) FROM sport";
        List<Map<String, Object>> databaseResults = jdbcTemplate.queryForList(generatedSql);
        return generateResponse(userQuestion, databaseResults);
    }

    private AskResponse generateResponse(String userQuestion, List<Map<String, Object>> databaseResults) {
        String answer = formatResult(userQuestion, databaseResults).answer();

        return AskResponse.builder()
                .answer(answer)
                .build();
    }

    private AskResponse formatResult(String userQuestion, List<Map<String, Object>> databaseResults) {
        String userPrompt = SUMMARY_USER_PROMPT_TEMPLATE.formatted(
                userQuestion,
                databaseResults.size(),
                databaseResults);

        return callLlm(SUMMARY_SYSTEM_PROMPT, userPrompt);
    }

    private String generateSql(String userQuestion) {
        String generatedSql = callLlm(sqlSystemPrompt, SQL_USER_PROMPT_TEMPLATE.formatted(userQuestion)).answer();
        validateSqlQuery(generatedSql);

        return generatedSql;
    }

    private void validateSqlQuery(String generatedSql) {
        String upperCaseSql = generatedSql.toUpperCase();

        if (!upperCaseSql.startsWith("SELECT")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed");
        }

        if (upperCaseSql.matches(".*\\b(DROP|DELETE|INSERT|UPDATE|TRUNCATE|ALTER|GRANT|COPY|CALL|DO)\\b.*")) {
            throw new IllegalArgumentException("Query contains forbidden SQL keywords");
        }
    }

    private AskResponse callLlm(String systemPrompt, String userPrompt) {
        AskResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .responseEntity(AskResponse.class)
                .getEntity();

        if (response == null) {
            throw new IllegalStateException("AI model returned an empty response");
        }

        return response;
    }
}
