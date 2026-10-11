package ee.sportclub.infrastructure.llm;

import com.anthropic.models.messages.OutputConfig;
import ee.sportclub.persistence.llmmodel.LlmModel;
import org.springframework.ai.anthropic.AnthropicCacheOptions;
import org.springframework.ai.anthropic.AnthropicCacheStrategy;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

// Päringu ChatOptions provideri järgi. Mudel ja temperatuur on kõigil ühised (ChatOptions), mõtlemistase aga
// provideri-spetsiifiline: Gemini thinkingLevel (MINIMAL/LOW/MEDIUM/HIGH), OpenAI reasoning_effort (minimal/low/...),
// Anthropic effort (low/medium/high/...; MINIMAL -> LOW, sest Anthropicul madalamat taset pole),
// DeepSeek thinking + reasoning_effort (MINIMAL -> mõtlemine välja, LOW -> low, MEDIUM -> high, HIGH -> max).
@Component
public class LlmChatOptionsFactory {

    // thinkingLevel: null = mudeli vaikimisi käitumine; temperature: null = jäetakse saatmata
    public ChatOptions.Builder<?> createChatOptionsBuilder(LlmModel llmModel, Double temperature, String thinkingLevel) {
        boolean sendThinkingLevel = thinkingLevel != null && llmModel.getSupportsThinkingLevel();
        ChatOptions.Builder<?> chatOptionsBuilder = switch (llmModel.getProvider()) {
            case LlmChatClients.GOOGLE_GENAI -> createGoogleGenAiChatOptionsBuilder(sendThinkingLevel, thinkingLevel);
            case LlmChatClients.OPENAI -> createOpenAiChatOptionsBuilder(sendThinkingLevel, thinkingLevel);
            case LlmChatClients.ANTHROPIC -> createAnthropicChatOptionsBuilder(sendThinkingLevel, thinkingLevel);
            case LlmChatClients.DEEPSEEK -> createDeepSeekChatOptionsBuilder(sendThinkingLevel, thinkingLevel);
            default -> ChatOptions.builder();
        };
        chatOptionsBuilder.model(llmModel.getName());
        if (temperature != null) {
            chatOptionsBuilder.temperature(temperature);
        }
        return chatOptionsBuilder;
    }

    private ChatOptions.Builder<?> createGoogleGenAiChatOptionsBuilder(boolean sendThinkingLevel, String thinkingLevel) {
        GoogleGenAiChatOptions.Builder googleGenAiChatOptionsBuilder = GoogleGenAiChatOptions.builder();
        if (sendThinkingLevel) {
            googleGenAiChatOptionsBuilder.thinkingLevel(GoogleGenAiThinkingLevel.valueOf(thinkingLevel));
        }
        return googleGenAiChatOptionsBuilder;
    }

    private ChatOptions.Builder<?> createOpenAiChatOptionsBuilder(boolean sendThinkingLevel, String thinkingLevel) {
        OpenAiChatOptions.Builder openAiChatOptionsBuilder = OpenAiChatOptions.builder();
        if (sendThinkingLevel) {
            openAiChatOptionsBuilder.reasoningEffort(thinkingLevel.toLowerCase(Locale.ROOT));
        }
        return openAiChatOptionsBuilder;
    }

    // Anthropic ei puhverda automaatselt: SYSTEM_ONLY paneb cache-murdepunkti system promptile (reeglid + andmestik),
    // nii loetakse jätkuküsimustel suurem osa sisendist vahemälust (5 min TTL, kirjutamine 1.25x, lugemine ~0.05-0.1x)
    private ChatOptions.Builder<?> createAnthropicChatOptionsBuilder(boolean sendThinkingLevel, String thinkingLevel) {
        AnthropicChatOptions.Builder anthropicChatOptionsBuilder = AnthropicChatOptions.builder();
        anthropicChatOptionsBuilder.cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                .build());
        if (sendThinkingLevel) {
            anthropicChatOptionsBuilder.effort(toAnthropicEffort(thinkingLevel));
        }
        return anthropicChatOptionsBuilder;
    }

    // DeepSeek (OpenAI vorming): thinking lülitatakse extra_body kaudu, sügavus reasoning_effort'iga (low/high/max)
    private ChatOptions.Builder<?> createDeepSeekChatOptionsBuilder(boolean sendThinkingLevel, String thinkingLevel) {
        OpenAiChatOptions.Builder deepSeekChatOptionsBuilder = OpenAiChatOptions.builder();
        if (!sendThinkingLevel) {
            return deepSeekChatOptionsBuilder;
        }
        if (thinkingLevel.equals("MINIMAL")) {
            deepSeekChatOptionsBuilder.extraBody(Map.of("thinking", Map.of("type", "disabled")));
            return deepSeekChatOptionsBuilder;
        }
        deepSeekChatOptionsBuilder.extraBody(Map.of("thinking", Map.of("type", "enabled")));
        deepSeekChatOptionsBuilder.reasoningEffort(toDeepSeekReasoningEffort(thinkingLevel));
        return deepSeekChatOptionsBuilder;
    }

    private String toDeepSeekReasoningEffort(String thinkingLevel) {
        return switch (thinkingLevel) {
            case "MEDIUM" -> "high";
            case "HIGH" -> "max";
            default -> "low";  // LOW
        };
    }

    private OutputConfig.Effort toAnthropicEffort(String thinkingLevel) {
        return switch (thinkingLevel) {
            case "MEDIUM" -> OutputConfig.Effort.MEDIUM;
            case "HIGH" -> OutputConfig.Effort.HIGH;
            default -> OutputConfig.Effort.LOW;  // MINIMAL, LOW
        };
    }
}
