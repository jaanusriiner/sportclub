package ee.sportclub.infrastructure.llm;

import java.math.BigDecimal;

// LLM kutse kulu eurodes; input sisaldab ka vahemälust loetud ja sinna kirjutatud tokenite kulu
public record LlmCost(BigDecimal inputCost, BigDecimal outputCost) {

    // mitme LLM kutse kulu kokku; null + null = null (hind teadmata)
    public LlmCost plus(LlmCost other) {
        return new LlmCost(sumNullable(inputCost, other.inputCost), sumNullable(outputCost, other.outputCost));
    }

    private static BigDecimal sumNullable(BigDecimal first, BigDecimal second) {
        if (first == null) {
            return second;
        }
        return second == null ? first : first.add(second);
    }

    public BigDecimal totalCost() {
        if (inputCost == null || outputCost == null) {
            return null;
        }
        return inputCost.add(outputCost);
    }
}
