package ee.sportclub.service.ask;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// SQL-režiimi vestluste olek mälus: kataloog (vestluse alguses loetud) ja vestluse jooksul tehtud päringute tulemused.
// Sama põhimõte nagu ConversationDataStore'il: backendi restardil kaob, siis alustatakse vestluse olekut uuesti.
@Component
public class SqlConversationStore {

    private static final int MAX_CONVERSATIONS = 200;

    private final Map<String, SqlConversation> conversationsById = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, SqlConversation> eldest) {
                    return size() > MAX_CONVERSATIONS;
                }
            });

    public SqlConversation saveNewConversation(String conversationId, String catalog) {
        SqlConversation sqlConversation = new SqlConversation(catalog, Collections.synchronizedList(new ArrayList<>()));
        conversationsById.put(conversationId, sqlConversation);
        return sqlConversation;
    }

    public Optional<SqlConversation> findConversationBy(String conversationId) {
        return Optional.ofNullable(conversationsById.get(conversationId));
    }

    public record SqlConversation(String catalog, List<SqlQueryResult> queryResults) {
    }

    // rowsCsv on tulemus kujul, mis läheb vastuse sammus LLM-ile
    public record SqlQueryResult(String sql, int rowCount, String rowsCsv) {
    }
}
