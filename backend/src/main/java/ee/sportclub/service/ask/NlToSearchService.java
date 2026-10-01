package ee.sportclub.service.ask;

import ee.sportclub.controller.ask.dto.AskResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NlToSearchService {

    private static final String SUMMARY_SYSTEM_PROMPT_TEMPLATE = """
            You are search engine for customers visiting Sportclub webpage.
            As input along with customer/user prompt, you will get database printout with all active trainings in system according to schema below
            User question might be in Estonian, use both Estonian and English language to search data.
            
            SCHEMA:
            SCHEMA:
            v_training_date_extended(
              training_date_id UUID,
              training_group_id UUID,
              training_group_name TEXT,
              training_group_description TEXT,
              sport_id UUID,
              sport_name TEXT,
              facility_id UUID,
              facility_name TEXT,
              facility_address TEXT,
              facility_description TEXT,
              area_id UUID,
              area_name TEXT,
              trainer_id UUID,
              trainer_name TEXT,
              trainer_email TEXT,
              sportclub_id UUID,
              sportclub_name TEXT,
              skill_level_id UUID,
              skill_level_name TEXT,
              training_date DATE,
              training_time TIME,
              training_date_duration INTEGER,
              status TEXT,
              user_count INTEGER,
              training_date_max_size INTEGER,
              training_weekdays TEXT,
              review_description TEXT
            )
            
            RULES:
            1. If you cannot answer from this schema, return: CANNOT_ANSWER
            """;

    private static final String SUMMARY_USER_PROMPT_TEMPLATE = """
            Summarize this database query result in 1-3 plain sentences.
            Don't mention SQL or technical terms. Be specific about numbers.
            
            Question: %s
            Results (%d rows): %s
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ChatClient chatClient;
    private final String SystemPrompt;

    public NlToSearchService(JdbcTemplate jdbcTemplate, ChatClient.Builder builder,
                             @Value("${nlsql.dialect}") String dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatClient = builder.build();
        this.SystemPrompt = SUMMARY_SYSTEM_PROMPT_TEMPLATE.formatted(dialect, dialect);
    }

    public AskResponse ask(String userQuestion) {
//        String generatedSql = generateSql(userQuestion);
        String generatedSql = "SELECT * FROM v_training_date_extended;";
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

        return callLlm(SUMMARY_SYSTEM_PROMPT_TEMPLATE, userPrompt);
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
