package ee.sportclub.service.ask;

import ee.sportclub.controller.ask.dto.AskHistoryEntry;
import ee.sportclub.controller.ask.dto.BenchmarkAskRequest;
import ee.sportclub.controller.ask.dto.BenchmarkAskResponse;
import ee.sportclub.infrastructure.llm.LlmChatClients;
import ee.sportclub.persistence.llmmodel.LlmModel;
import ee.sportclub.persistence.tone.Tone;
import ee.sportclub.service.llmmodel.LlmModelService;
import ee.sportclub.service.tone.ToneService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
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
            You are search engine for customers visiting Sportclub webpage.
            As input along with customer/user prompt, you will get database printout with all active trainings from current date forward in system according to schema below
            User question might be in Estonian, use both Estonian and English language to search data.
            
            Before classifying, reason step by step in the “reasoning” field: (1) Is the question related to sport and training options? (2) Can user question be answered from provided dataset and schema? (3) Can customer be even answered before asking additional clarifying questions.
            
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
            1. If you cannot answer from this schema, return: Põnev küsimus :) , aga kahjuks minu teadmiste väline
            """;

    private static final String DATASET_TEMPLATE = """
            
            DATA (%d rows):
            %s
            """;

    private static final String SUMMARY_USER_PROMPT_TEMPLATE = """
            Answer this question in 1-3 plain sentences using the DATA in the system prompt.
            Don't mention SQL or technical terms. Be specific about numbers.
            
            Question: %s
            """;

    private static final String DATASET_SQL = "SELECT * FROM v_training_date_extended WHERE training_date >= CURRENT_DATE;";

    private static final BigDecimal TOKENS_PER_PRICE_UNIT = BigDecimal.valueOf(1_000_000);
    private static final int COST_SCALE = 6;

    private final JdbcTemplate jdbcTemplate;
    private final LlmChatClients llmChatClients;
    private final LlmModelService llmModelService;
    private final ToneService toneService;
    private final ConversationDataStore conversationDataStore;
    private final BigDecimal usdToEurRate;

    public NlToSearchService2(JdbcTemplate jdbcTemplate, LlmChatClients llmChatClients,
                              LlmModelService llmModelService, ToneService toneService,
                              ConversationDataStore conversationDataStore,
                              @Value("${llm.usd-to-eur-rate}") BigDecimal usdToEurRate) {
        this.jdbcTemplate = jdbcTemplate;
        this.llmChatClients = llmChatClients;
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
        List<Map<String, Object>> databaseResults = jdbcTemplate.queryForList(DATASET_SQL);
        String dataset = DATASET_TEMPLATE.formatted(databaseResults.size(), databaseResults);
        conversationDataStore.saveDataset(request.conversationId(), dataset);
        return dataset;
    }

    private BenchmarkAskResponse generateResponse(BenchmarkAskRequest request, LlmModel llmModel, Tone tone,
                                                 String dataset) {
        String userPrompt = SUMMARY_USER_PROMPT_TEMPLATE.formatted(request.question());
        ChatOptions.Builder<?> chatOptionsBuilder = ChatOptions.builder()
                .model(llmModel.getName())
                .temperature(getAllowedTemperature(request.temperature(), llmModel));

        ResponseEntity<ChatResponse, LlmAnswer> responseEntity = callLlm(llmModel, chatOptionsBuilder,
                getSystemPrompt(dataset, tone), toHistoryMessages(request.history()), userPrompt);

        Usage usage = responseEntity.getResponse().getMetadata().getUsage();
        Integer inputTokens = usage.getPromptTokens();
        Integer cachedTokens = getCachedTokens(usage);
        Integer outputTokens = usage.getCompletionTokens();

        return BenchmarkAskResponse.builder()
                .answer(responseEntity.getEntity().answer())
                .reasoning(responseEntity.getEntity().reasoning())
                .inputTokens(inputTokens)
                .cachedTokens(cachedTokens)
                .outputTokens(outputTokens)
                .inputCost(calculateInputCostEur(inputTokens, cachedTokens, llmModel))
                .outputCost(calculateCostEur(outputTokens, llmModel.getOutputPriceUsd()))
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

    // provider ei pruugi vahemälu infot tagastada - siis loeme, et vahemälust ei tulnud midagi
    private Integer getCachedTokens(Usage usage) {
        Long cacheReadInputTokens = usage.getCacheReadInputTokens();
        return cacheReadInputTokens == null ? 0 : cacheReadInputTokens.intValue();
    }

    // inputTokens sisaldab cached tokeneid: need arvestatakse soodushinnaga, ülejäänud tavahinnaga
    private BigDecimal calculateInputCostEur(Integer inputTokens, Integer cachedTokens, LlmModel llmModel) {
        if (inputTokens == null) {
            return null;
        }
        int uncachedTokens = inputTokens - cachedTokens;
        return calculateCostEur(uncachedTokens, llmModel.getInputPriceUsd())
                .add(calculateCostEur(cachedTokens, llmModel.getCachedInputPriceUsd()));
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
