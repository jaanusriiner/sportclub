package ee.sportclub.infrastructure.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

// Spring AI autokonfigureeritud ChatClient.Builder on välja lülitatud (spring.ai.chat.client.enabled=false),
// sest mitme provideri (mitme ChatModel beani) korral ei tea Spring, millist mudelit kasutada.
// Vaikimisi builder on Gemini oma - seda kasutavad /api/ask teenused (NlToSearchService, NlToSqlService).
@Configuration
public class LlmConfig {

    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder(GoogleGenAiChatModel googleGenAiChatModel) {
        return ChatClient.builder(googleGenAiChatModel);
    }
}
