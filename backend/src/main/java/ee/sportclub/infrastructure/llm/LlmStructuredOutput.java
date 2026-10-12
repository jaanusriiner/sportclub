package ee.sportclub.infrastructure.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.converter.BeanOutputConverter;
import tools.jackson.core.JacksonException;

import java.util.List;
import java.util.function.Function;

// LLM-i JSON-vastuse teisendamine Java objektiks kõigi providerite jaoks ühtemoodi.
// Miks mitte ChatClient...responseEntity(Class)? See teisendab ainult ESIMESE Generation'i (getResult()). Anthropic
// mõtlevad mudelid (nt Sonnet 5.5) tagastavad aga enne vastust mõtlemisploki(d), millest Spring AI teeb eraldi
// Generation'i - vaikimisi tühja tekstiga -> teisendus ebaõnnestub ("No content to map due to end-of-input").
// Siin võtame viimase mittetühja teksti, mis on mudeli lõplik vastus.
@Slf4j
public final class LlmStructuredOutput {

    private LlmStructuredOutput() {
    }

    // plainTextFallback: kui mudel vastas tavatekstina, mitte JSON-ina (nt jäljendas jätkuküsimusel ajaloo vormingut),
    // teisendatakse tekst selle funktsiooniga; null = sellisel juhul viga
    public static <T> T toEntity(ChatResponse chatResponse, BeanOutputConverter<T> outputConverter, String modelName,
                                 Function<String, T> plainTextFallback) {
        String finalText = getFinalText(chatResponse);
        if (finalText == null) {
            // nt Anthropic Opus/Sonnet 5.5 turvaklassifikaatori keeldumine: HTTP 200, stop_reason=refusal, sisu puudub
            throw new IllegalStateException("AI model " + modelName
                    + " returned no content - the provider most likely declined the request (safety refusal)");
        }
        T entity;
        try {
            entity = outputConverter.convert(finalText);
        } catch (JacksonException exception) {
            if (plainTextFallback == null) {
                throw exception;
            }
            log.warn("[LlmStructuredOutput] model={} vastas JSON-i asemel tavatekstina - kasutame teksti vastusena: {}",
                    modelName, exception.getOriginalMessage());
            entity = plainTextFallback.apply(finalText);
        }
        if (entity == null) {
            throw new IllegalStateException("AI model " + modelName + " returned an empty answer");
        }
        return entity;
    }

    // mõtlemisplokid on enne vastust; lõplik vastus on viimane Generation, millel on tekst
    private static String getFinalText(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResults() == null) {
            return null;
        }
        List<Generation> generations = chatResponse.getResults();
        for (int index = generations.size() - 1; index >= 0; index--) {
            String text = generations.get(index).getOutput().getText();
            if (text != null && !text.isBlank()) {
                return text;
            }
        }
        return null;
    }
}
