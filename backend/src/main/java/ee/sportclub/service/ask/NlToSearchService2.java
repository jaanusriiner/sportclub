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
import ee.sportclub.service.llmmodel.LlmModelService;
import ee.sportclub.service.tone.ToneService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NlToSearchService2 {

    private static final String SUMMARY_SYSTEM_PROMPT_TEMPLATE = """
            You are customer support agent for customers visiting Sportclub webpage.
            As input along with customer/user prompt, you will get database printout with all active trainings from current date forward in system according to schema below
            User question might be in Estonian, use both Estonian and English language to search data but always respond to customer in language they started conversation.
            
            Once user shows interest to join trainings, guide user first to Register to system as new user (or login as existing one) to complete the registration to traininggroup and training.
            You are not capable to register user yourself to training. User has to do it himself after logging into portal. But do not repeat it in conversation on every response if history already contains such response. Keep some distance before suggesting it again.
            When getting closer on proposing training time and facility, always bring our trainer name to praise his/her competence.
            
            In the “reasoning” field, briefly explain which data the answer is based on: whether the question is related to sport and training options, whether it can be answered from the provided dataset, and whether a clarifying question to the customer is needed instead.
            
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
            Answer this question in 1-3 plain sentences using the DATA in the system or user prompt.
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

    private final JdbcTemplate jdbcTemplate;
    private final LlmChatClients llmChatClients;
    private final LlmChatOptionsFactory llmChatOptionsFactory;
    private final LlmModelService llmModelService;
    private final ToneService toneService;
    private final ConversationDataStore conversationDataStore;
    private final BenchmarkConversationMemory benchmarkConversationMemory;
    private final LlmCostCalculator llmCostCalculator;

    public NlToSearchService2(JdbcTemplate jdbcTemplate, LlmChatClients llmChatClients,
                              LlmChatOptionsFactory llmChatOptionsFactory,
                              LlmModelService llmModelService, ToneService toneService,
                              ConversationDataStore conversationDataStore, LlmCostCalculator llmCostCalculator,
                              BenchmarkConversationMemory benchmarkConversationMemory) {
        this.jdbcTemplate = jdbcTemplate;
        this.llmChatClients = llmChatClients;
        this.llmChatOptionsFactory = llmChatOptionsFactory;
        this.llmModelService = llmModelService;
        this.toneService = toneService;
        this.conversationDataStore = conversationDataStore;
        this.benchmarkConversationMemory = benchmarkConversationMemory;
        this.llmCostCalculator = llmCostCalculator;
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
        boolean isFirstQuestion = benchmarkConversationMemory.isNewConversation(request.conversationId());
        if (!isFirstQuestion) {
            Optional<String> savedDataset = conversationDataStore.findDatasetBy(request.conversationId());
            if (savedDataset.isPresent()) {
                return savedDataset.get();
            }
        }
        List<Map<String, Object>> facilities = jdbcTemplate.queryForList(FACILITIES_SQL);
        List<Map<String, Object>> trainings = jdbcTemplate.queryForList(TRAININGS_SQL);
        String dataset = DATASET_TEMPLATE.formatted(facilities.size(), CsvFormatter.toCsv(facilities), trainings.size(),
                CsvFormatter.toCsv(trainings));
        conversationDataStore.saveDataset(request.conversationId(), dataset);
        return dataset;
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
                getSystemPrompt(dataset, tone), benchmarkConversationMemory.getHistoryMessages(request.conversationId()), userPrompt,
                request.conversationId());

        LlmTokenUsage tokenUsage = LlmTokenUsage.from(responseEntity.getResponse().getMetadata().getUsage());
        LlmCost cost = llmCostCalculator.calculateCost(tokenUsage, llmModel);
        benchmarkConversationMemory.saveExchange(request.conversationId(), request.question(),
                responseEntity.getEntity().answer());

        return BenchmarkAskResponse.builder()
                .answer(responseEntity.getEntity().answer())
                .reasoning(responseEntity.getEntity().reasoning())
                .inputTokens(tokenUsage.inputTokens())
                .cachedTokens(tokenUsage.cachedTokens())
                .outputTokens(tokenUsage.outputTokens())
                .thinkingTokens(tokenUsage.thinkingTokens())
                .inputCost(cost.inputCost())
                .outputCost(cost.outputCost())
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

    private ResponseEntity<ChatResponse, LlmAnswer> callLlm(LlmModel llmModel, ChatOptions.Builder<?> chatOptionsBuilder,
                                                            String systemPrompt, List<Message> historyMessages,
                                                            String userPrompt, String conversationId) {
        ChatClient chatClient = llmChatClients.getChatClientBy(llmModel.getProvider());
        // JSON-vormingu juhis system prompti lõppu (muutumatu, vahemällu sobiv); vastuse teisendame ise, vt LlmStructuredOutput
        BeanOutputConverter<LlmAnswer> outputConverter = new BeanOutputConverter<>(LlmAnswer.class);
        ChatResponse chatResponse = chatClient.prompt()
                .options(chatOptionsBuilder)
                // MetricsAdvisor seob logirea mudeli hindade ja Benchmarki paneeli vestlusega
                .advisors(advisorSpec -> advisorSpec
                        .param(MetricsAdvisor.LLM_MODEL_ID, llmModel.getId())
                        .param(MetricsAdvisor.CONVERSATION_ID, conversationId))
                .system(systemPrompt + "\n\n" + outputConverter.getFormat())
                .messages(historyMessages)
                .user(userPrompt)
                .call()
                .chatResponse();

        LlmAnswer llmAnswer = LlmStructuredOutput.toEntity(chatResponse, outputConverter, llmModel.getName());
        return new ResponseEntity<>(chatResponse, llmAnswer);
    }
}
