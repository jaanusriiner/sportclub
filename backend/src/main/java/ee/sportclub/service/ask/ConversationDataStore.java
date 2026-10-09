package ee.sportclub.service.ask;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// Benchmark-vestluse andmestik (LLM-ile antav andmebaasi väljavõte) vestluse id järgi.
// Andmestik päritakse vestluse esimesel küsimusel ja jääb samaks kogu vestluse ajaks - nii on system prompti
// algus vestluse jooksul muutumatu ja provider saab seda vahemälust (prompt caching) odavamalt arvestada.
// Hoitakse mälus: backendi restardil kaob, siis päritakse andmestik uuesti.
@Component
public class ConversationDataStore {

    // vanimad vestlused visatakse välja, et mälu ei kasvaks piiramatult
    private static final int MAX_CONVERSATIONS = 200;

    private final Map<String, String> datasetsByConversationId = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > MAX_CONVERSATIONS;
                }
            });

    public void saveDataset(String conversationId, String dataset) {
        datasetsByConversationId.put(conversationId, dataset);
    }

    public Optional<String> findDatasetBy(String conversationId) {
        return Optional.ofNullable(datasetsByConversationId.get(conversationId));
    }
}
