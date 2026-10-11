package ee.sportclub.infrastructure.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
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
    public ChatClient.Builder chatClientBuilder(GoogleGenAiChatModel googleGenAiChatModel, MetricsAdvisor metricsAdvisor) {
        return ChatClient.builder(googleGenAiChatModel).defaultAdvisors(metricsAdvisor);
    }

    // Benchmarki vestluse ajalugu hoiab server (mitte klient), et seda ei saaks päringus võltsida ega paisutada.
    // Mälus hoidla kaob restardil; aken piirab ajaloo viimase N sõnumiga (= piirab ka input tokeneid).
    @Bean
    public ChatMemory chatMemory(@Value("${llm.chat-memory.max-messages}") int maxMessages) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(maxMessages)
                .build();
    }
}
