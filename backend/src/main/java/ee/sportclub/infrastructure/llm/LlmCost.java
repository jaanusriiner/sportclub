package ee.sportclub.infrastructure.llm;

import java.math.BigDecimal;

// LLM kutse kulu eurodes; input sisaldab ka vahemälust loetud ja sinna kirjutatud tokenite kulu
public record LlmCost(BigDecimal inputCost, BigDecimal outputCost) {

    public BigDecimal totalCost() {
        if (inputCost == null || outputCost == null) {
            return null;
        }
        return inputCost.add(outputCost);
    }
}
