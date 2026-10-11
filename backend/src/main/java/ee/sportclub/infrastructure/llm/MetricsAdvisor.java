package ee.sportclub.infrastructure.llm;

import ee.sportclub.persistence.llmmodel.LlmModel;
import ee.sportclub.persistence.llmmodel.LlmModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// Logib iga LLM kutse kohta tokenid, kulu (EUR, mudeli hindadega llm_model tabelist), kestuse ja lõpetamise põhjuse.
// Lisatakse kõigile ChatClient'idele (LlmChatClients, LlmConfig), seega katab nii Benchmarki kui Küsi päringud.
// Kutsuja saab advisor'i parameetritega anda kaasa mudeli id ja vestluse id (LLM_MODEL_ID, CONVERSATION_ID).
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsAdvisor implements CallAdvisor {

    public static final String LLM_MODEL_ID = "metrics.llmModelId";
    public static final String CONVERSATION_ID = "metrics.conversationId";
    // mitme sammuga voo (nt SQL-režiim: sql-decision, answer) samm, et logiread oleks eristatavad
    public static final String STEP = "metrics.step";

    private final LlmModelRepository llmModelRepository;
    private final LlmCostCalculator llmCostCalculator;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long startNanos = System.nanoTime();
        Optional<LlmModel> llmModel = findLlmModel(request);
        Object conversationId = request.context().get(CONVERSATION_ID);
        Object step = request.context().get(STEP);
        try {
            ChatClientResponse response = chain.nextCall(request);  // kutse läheb ahelas edasi (lõpuks mudelini)
            logSuccess(response, llmModel, conversationId, step, elapsedMillis(startNanos));
            return response;
        } catch (RuntimeException exception) {
            // ka ebaõnnestunud kutse (nt ajalõpp) on oluline mõõdik - logime ja viskame edasi
            log.warn("[LlmMetrics] llm_call status=error model={} conversationId={} step={} latencyMs={} error={}",
                    llmModel.map(LlmModel::getName).orElse(getRequestedModelName(request)), conversationId, step,
                    elapsedMillis(startNanos), exception.toString());
            throw exception;
        }
    }

    private void logSuccess(ChatClientResponse response, Optional<LlmModel> llmModel, Object conversationId,
                            Object step, long latencyMillis) {
        ChatResponse chatResponse = response.chatResponse();
        Usage usage = chatResponse == null ? null : chatResponse.getMetadata().getUsage();
        if (usage == null) {
            log.warn("[LlmMetrics] llm_call status=empty model={} conversationId={} step={} latencyMs={}",
                    llmModel.map(LlmModel::getName).orElse(null), conversationId, step, latencyMillis);
            return;
        }
        LlmTokenUsage tokenUsage = LlmTokenUsage.from(usage);
        if (llmModel.isEmpty() && chatResponse.getMetadata().getModel() != null) {
            // päringus mudelit polnud (vaikemudel, nt Küsi vaade) - proovime vastuses teatatud mudeli nime järgi
            llmModel = llmModelRepository.findLlmModelBy(chatResponse.getMetadata().getModel());
        }
        LlmCost cost = llmModel.map(model -> llmCostCalculator.calculateCost(tokenUsage, model))
                .orElse(new LlmCost(null, null));  // tundmatu mudel (nt pole tabelis) - tokenid logime ikka
        String finishReason = getFinishReason(chatResponse);

        log.info("[LlmMetrics] llm_call status=ok provider={} model={} conversationId={} step={} latencyMs={} "
                        + "in={} cached={} cacheWrite={} out={} thinking={} inputCostEur={} outputCostEur={} "
                        + "totalCostEur={} finishReason={}",
                llmModel.map(LlmModel::getProvider).orElse(null),
                llmModel.map(LlmModel::getName).orElse(chatResponse.getMetadata().getModel()),
                conversationId, step, latencyMillis,
                tokenUsage.inputTokens(), tokenUsage.cachedTokens(), tokenUsage.cacheWriteTokens(),
                tokenUsage.outputTokens(), tokenUsage.thinkingTokens(),
                cost.inputCost(), cost.outputCost(), cost.totalCost(), finishReason);

        if (isTruncated(finishReason)) {
            log.warn("[LlmMetrics] llm_call vastus katkes tokenite piiri tõttu: model={} conversationId={} out={}",
                    llmModel.map(LlmModel::getName).orElse(null), conversationId, tokenUsage.outputTokens());
        }
    }

    // eelistame kutsuja antud id-d; muidu päringus küsitud mudeli nime (nt Küsi vaate Gemini vaikemudel)
    private Optional<LlmModel> findLlmModel(ChatClientRequest request) {
        if (request.context().get(LLM_MODEL_ID) instanceof Integer llmModelId) {
            return llmModelRepository.findById(llmModelId);
        }
        String requestedModelName = getRequestedModelName(request);
        return requestedModelName == null ? Optional.empty() : llmModelRepository.findLlmModelBy(requestedModelName);
    }

    private String getRequestedModelName(ChatClientRequest request) {
        ChatOptions chatOptions = request.prompt().getOptions();
        return chatOptions == null ? null : chatOptions.getModel();
    }

    // viimane Generation = mudeli lõplik vastus (Anthropicu mõtlemisplokid on eraldi Generation'id selle ees)
    private String getFinishReason(ChatResponse chatResponse) {
        if (chatResponse.getResults() == null || chatResponse.getResults().isEmpty()) {
            return null;
        }
        return chatResponse.getResults().getLast().getMetadata().getFinishReason();
    }

    // providerid nimetavad sama asja erinevalt: Gemini MAX_TOKENS, OpenAI/DeepSeek length, Anthropic max_tokens
    private boolean isTruncated(String finishReason) {
        if (finishReason == null) {
            return false;
        }
        String normalizedFinishReason = finishReason.toUpperCase(Locale.ROOT);
        return normalizedFinishReason.equals("MAX_TOKENS") || normalizedFinishReason.equals("LENGTH");
    }

    private long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    @Override
    public String getName() {
        return "llm-metrics";
    }

    // kõrgeim prioriteet = välimine advisor: mõõdab kogu ahela (ka teiste advisor'ite) kestuse
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
