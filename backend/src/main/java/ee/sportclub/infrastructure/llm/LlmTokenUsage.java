package ee.sportclub.infrastructure.llm;

import com.openai.core.JsonValue;
import com.openai.models.completions.CompletionUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.google.genai.metadata.GoogleGenAiUsage;

// LLM vastuse tokenid ühtlustatult üle providerite:
// - inputTokens sisaldab cachedTokens'it ja cacheWriteTokens'it
// - cacheWriteTokens: ainult Anthropic (vahemällu kirjutamine maksab 1.25x input hinda), teistel 0
// - outputTokens sisaldab thinkingTokens'it (mõlemad providerid arveldavad mõtlemist output hinnaga)
// Gemini completionTokens EI sisalda mõtlemistokeneid (need on eraldi thoughtsTokenCount) - liidame need juurde.
// OpenAI completion_tokens sisaldab reasoning tokeneid juba ise.
// Anthropic input_tokens (Spring AI promptTokens) EI sisalda vahemälust loetud ega sinna kirjutatud tokeneid - liidame
// need juurde; output_tokens sisaldab mõtlemist, aga eraldi mõtlemistokenite arvu Anthropic ei anna (thinkingTokens null).
public record LlmTokenUsage(Integer inputTokens, Integer cachedTokens, Integer cacheWriteTokens, Integer outputTokens,
                            Integer thinkingTokens) {

    public static LlmTokenUsage from(Usage usage) {
        Integer inputTokens = usage.getPromptTokens();
        Integer cachedTokens = getCachedTokens(usage);
        if (usage instanceof GoogleGenAiUsage googleGenAiUsage) {
            int thinkingTokens = zeroIfNull(googleGenAiUsage.getThoughtsTokenCount());
            Integer outputTokens = usage.getCompletionTokens() == null ? null : usage.getCompletionTokens() + thinkingTokens;
            return new LlmTokenUsage(inputTokens, cachedTokens, 0, outputTokens, thinkingTokens);
        }
        if (usage.getNativeUsage() instanceof com.anthropic.models.messages.Usage) {
            int cacheWriteTokens = usage.getCacheWriteInputTokens() == null ? 0 : usage.getCacheWriteInputTokens().intValue();
            Integer totalInputTokens = inputTokens == null ? null : inputTokens + cachedTokens + cacheWriteTokens;
            return new LlmTokenUsage(totalInputTokens, cachedTokens, cacheWriteTokens, usage.getCompletionTokens(), null);
        }
        return new LlmTokenUsage(inputTokens, cachedTokens, 0, usage.getCompletionTokens(), getOpenAiReasoningTokens(usage));
    }

    // provider ei pruugi vahemälu infot tagastada - siis loeme, et vahemälust ei tulnud midagi
    private static Integer getCachedTokens(Usage usage) {
        Long cacheReadInputTokens = usage.getCacheReadInputTokens();
        if ((cacheReadInputTokens == null || cacheReadInputTokens == 0)
                && usage.getNativeUsage() instanceof CompletionUsage completionUsage) {
            return getDeepSeekCacheHitTokens(completionUsage);
        }
        return cacheReadInputTokens == null ? 0 : cacheReadInputTokens.intValue();
    }

    // DeepSeek (OpenAI kliendi kaudu) teatab vahemälu tabamused mitte-standardse väljaga prompt_cache_hit_tokens,
    // mida OpenAI SDK hoiab "lisaväljade" all
    private static Integer getDeepSeekCacheHitTokens(CompletionUsage completionUsage) {
        JsonValue cacheHitTokens = completionUsage._additionalProperties().get("prompt_cache_hit_tokens");
        if (cacheHitTokens == null) {
            return 0;
        }
        Object cacheHitTokenCount = cacheHitTokens.asNumber().orElse(null);
        return cacheHitTokenCount instanceof Number number ? number.intValue() : 0;
    }

    private static Integer getOpenAiReasoningTokens(Usage usage) {
        if (usage.getNativeUsage() instanceof CompletionUsage completionUsage) {
            return completionUsage.completionTokensDetails()
                    .flatMap(CompletionUsage.CompletionTokensDetails::reasoningTokens)
                    .map(Long::intValue)
                    .orElse(0);
        }
        return 0;
    }

    private static int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }
}
