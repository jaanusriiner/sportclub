package ee.sportclub.infrastructure.llm;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;

// Provideri nimi (llm_model.provider) -> selle provideri ChatClient.
// Mudel ise valitakse päringu kaupa ChatOptions'iga, seega ühe provideri kohta piisab ühest ChatClient'ist.
// Uue provideri lisamiseks: Spring AI starter + API võti application.properties'is + rida siia.
@Component
public class LlmChatClients {

    public static final String GOOGLE_GENAI = "google-genai";
    public static final String OPENAI = "openai";
    public static final String ANTHROPIC = "anthropic";

    private final Map<String, ChatClient> chatClientsByProvider;

    public LlmChatClients(GoogleGenAiChatModel googleGenAiChatModel, OpenAiChatModel openAiChatModel,
                          AnthropicChatModel anthropicChatModel) {
        this.chatClientsByProvider = Map.of(
                GOOGLE_GENAI, ChatClient.builder(googleGenAiChatModel).build(),
                OPENAI, ChatClient.builder(openAiChatModel).build(),
                ANTHROPIC, ChatClient.builder(anthropicChatModel).build());
    }

    public ChatClient getChatClientBy(String provider) {
        ChatClient chatClient = chatClientsByProvider.get(provider);
        if (chatClient == null) {
            throw new IllegalStateException("LLM provider is not configured: " + provider);
        }
        return chatClient;
    }
}
