package ee.sportclub.service.ask;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// LLM-i struktureeritud väljund. Eraldi BenchmarkAskResponse'ist, et tokeni- ja kuluväljad ei satuks LLM-ile
// antavasse JSON-skeemi (need täidab backend ise vastuse metaandmetest).
// reasoning on JSON-skeemis esimene: lühike selgitus, millele vastus tugineb. Sõnastus on teadlikult "explain", mitte
// "step-by-step reasoning" - Anthropic Opus/Sonnet 5.5 turvaklassifikaator (reasoning_extraction) võib mudeli
// sisemise arutluse väljapressimise katset keelduda (tühi vastus). Sügavat arutlust teeb mudel niikuinii sisemiselt.
@JsonPropertyOrder({"reasoning", "answer"})
public record LlmAnswer(

        @JsonPropertyDescription("Brief explanation of which data the answer is based on.")
        String reasoning,

        @JsonPropertyDescription("Final answer to the customer.")
        String answer

) {}
