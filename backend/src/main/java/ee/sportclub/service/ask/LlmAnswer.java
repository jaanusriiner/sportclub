package ee.sportclub.service.ask;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// LLM-i struktureeritud väljund. Eraldi BenchmarkAskResponse'ist, et tokeni- ja kuluväljad ei satuks LLM-ile
// antavasse JSON-skeemi (need täidab backend ise vastuse metaandmetest).
// reasoning on JSON-skeemis esimene, et LLM genereeriks arutluse enne vastust (vastus tugineb arutlusele).
@JsonPropertyOrder({"reasoning", "answer"})
public record LlmAnswer(

        @JsonPropertyDescription("Step-by-step reasoning done before answering. Fill this first.")
        String reasoning,

        @JsonPropertyDescription("Final answer to the customer, based on the reasoning.")
        String answer

) {}
