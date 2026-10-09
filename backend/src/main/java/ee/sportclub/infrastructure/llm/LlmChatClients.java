package ee.sportclub.infrastructure.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

// Provideri nimi (llm_model.provider) -> selle provideri ChatClient.
// Mudel ise valitakse päringu kaupa ChatOptions'iga, seega ühe provideri kohta piisab ühest ChatClient'ist.
@Component
public class LlmChatClients {

    public static final String GOOGLE_GENAI = "google-genai";

    private final Map<String, ChatClient> chatClientsByProvider;

    public LlmChatClients(ChatClient.Builder googleGenAiChatClientBuilder) {
        this.chatClientsByProvider = Map.of(GOOGLE_GENAI, googleGenAiChatClientBuilder.build());
    }

    public ChatClient getChatClientBy(String provider) {
        ChatClient chatClient = chatClientsByProvider.get(provider);
        if (chatClient == null) {
            throw new IllegalStateException("LLM provider is not configured: " + provider);
        }
        return chatClient;
    }
}
