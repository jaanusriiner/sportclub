package ee.sportclub.service.ask;

// LLM-i struktureeritud väljund. Eraldi BenchmarkAskResponse'ist, et tokeni- ja kuluväljad ei satuks LLM-ile
// antavasse JSON-skeemi (need täidab backend ise vastuse metaandmetest).
public record LlmAnswer(String answer) {}
