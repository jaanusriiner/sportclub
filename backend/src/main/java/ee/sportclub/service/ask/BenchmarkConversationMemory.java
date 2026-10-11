package ee.sportclub.service.ask;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

// Benchmarki vestluse ajalugu serveris (Spring AI ChatMemory) vestluse id järgi.
// Ajalukku läheb ainult puhas paar: kasutaja küsimus + lõplik vastus. Promptimallid, andmestik ja mudeli JSON-vastus
// (reasoning) sinna ei lähe - seepärast kasutame ChatMemory't otse, mitte MessageChatMemoryAdvisor'it, mis salvestaks
// iga LLM kutse täies mahus (SQL-režiimis ka päringu otsuse sammu).
@Component
@RequiredArgsConstructor
public class BenchmarkConversationMemory {

    private final ChatMemory chatMemory;

    // uus vestlus = selle id-ga pole veel ühtegi vastust (ka siis, kui ajalugu kadus backendi restardiga)
    public boolean isNewConversation(String conversationId) {
        return chatMemory.get(conversationId).isEmpty();
    }

    public List<Message> getHistoryMessages(String conversationId) {
        return chatMemory.get(conversationId);
    }

    // salvestatakse ainult õnnestunud vastus - ebaõnnestunud küsimus ei sega järgmisi
    public void saveExchange(String conversationId, String question, String answer) {
        chatMemory.add(conversationId, List.of(new UserMessage(question), new AssistantMessage(answer)));
    }
}
