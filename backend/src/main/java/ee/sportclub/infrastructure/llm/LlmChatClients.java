package ee.sportclub.infrastructure.llm;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

// Provideri nimi (llm_model.provider) -> selle provideri ChatClient.
// Mudel ise valitakse päringu kaupa ChatOptions'iga, seega ühe provideri kohta piisab ühest ChatClient'ist.
// Uue provideri lisamiseks: Spring AI starter + API võti application.properties'is + rida siia.
@Component
public class LlmChatClients {

    public static final String GOOGLE_GENAI = "google-genai";
    public static final String OPENAI = "openai";
    public static final String ANTHROPIC = "anthropic";
    public static final String DEEPSEEK = "deepseek";

    private final Map<String, ChatClient> chatClientsByProvider;

    public LlmChatClients(GoogleGenAiChatModel googleGenAiChatModel, OpenAiChatModel openAiChatModel,
                          AnthropicChatModel anthropicChatModel, MetricsAdvisor metricsAdvisor,
                          @Value("${llm.deepseek.base-url}") String deepSeekBaseUrl,
                          @Value("${llm.deepseek.api-key}") String deepSeekApiKey,
                          @Value("${llm.deepseek.timeout-seconds}") long deepSeekTimeoutSeconds) {
        this.chatClientsByProvider = Map.of(
                GOOGLE_GENAI, ChatClient.builder(googleGenAiChatModel).defaultAdvisors(metricsAdvisor).build(),
                OPENAI, ChatClient.builder(openAiChatModel).defaultAdvisors(metricsAdvisor).build(),
                ANTHROPIC, ChatClient.builder(anthropicChatModel).defaultAdvisors(metricsAdvisor).build(),
                DEEPSEEK, ChatClient.builder(createDeepSeekChatModel(deepSeekBaseUrl, deepSeekApiKey,
                        Duration.ofSeconds(deepSeekTimeoutSeconds))).defaultAdvisors(metricsAdvisor).build());
    }

    // DeepSeek API on OpenAI-ühilduv: sama OpenAiChatModel, aga oma aadressi ja võtmega. Ei registreeri seda Spring
    // beanina, sest siis jätaks OpenAI autokonfiguratsioon (@ConditionalOnMissingBean) päris OpenAI mudeli loomata.
    // ajalõpp pikem kui Spring AI vaikimisi 60 s (mõtlemine + suur andmestik) ja kordusi ei tehta, et katkenud kallist
    // päringut ei saadetaks ega arveldataks mitu korda
    private static OpenAiChatModel createDeepSeekChatModel(String baseUrl, String apiKey, Duration timeout) {
        return OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(baseUrl)
                        .apiKey(apiKey)
                        .model("deepseek-flash")
                        .timeout(timeout)
                        .maxRetries(0)
                        .build())
                .build();
    }

    public ChatClient getChatClientBy(String provider) {
        ChatClient chatClient = chatClientsByProvider.get(provider);
        if (chatClient == null) {
            throw new IllegalStateException("LLM provider is not configured: " + provider);
        }
        return chatClient;
    }
}
