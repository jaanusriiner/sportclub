package ee.sportclub.infrastructure.llm;

import ee.sportclub.persistence.llmmodel.LlmModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Ühine kulu arvutus (Benchmarki vastus ja MetricsAdvisor'i logi kasutavad sama valemit).
// Hinnad on llm_model tabelis USD-s 1M tokeni kohta; tulemus on EUR-s.
@Component
public class LlmCostCalculator {

    private static final BigDecimal TOKENS_PER_PRICE_UNIT = BigDecimal.valueOf(1_000_000);
    private static final int COST_SCALE = 6;

    private final BigDecimal usdToEurRate;

    public LlmCostCalculator(@Value("${llm.usd-to-eur-rate}") BigDecimal usdToEurRate) {
        this.usdToEurRate = usdToEurRate;
    }

    public LlmCost calculateCost(LlmTokenUsage tokenUsage, LlmModel llmModel) {
        return new LlmCost(
                calculateInputCostEur(tokenUsage, llmModel),
                calculateCostEur(tokenUsage.outputTokens(), llmModel.getOutputPriceUsd()));
    }

    // inputTokens sisaldab cached ja cache write tokeneid: cached soodushinnaga, cache write (Anthropic) kirjutamise
    // hinnaga, ülejäänud tavahinnaga
    private BigDecimal calculateInputCostEur(LlmTokenUsage tokenUsage, LlmModel llmModel) {
        if (tokenUsage.inputTokens() == null) {
            return null;
        }
        int cacheWriteTokens = tokenUsage.cacheWriteTokens();
        int uncachedTokens = tokenUsage.inputTokens() - tokenUsage.cachedTokens() - cacheWriteTokens;
        BigDecimal cacheWritePriceUsd = llmModel.getCacheWritePriceUsd() != null
                ? llmModel.getCacheWritePriceUsd()
                : llmModel.getInputPriceUsd();
        return calculateCostEur(uncachedTokens, llmModel.getInputPriceUsd())
                .add(calculateCostEur(tokenUsage.cachedTokens(), llmModel.getCachedInputPriceUsd()))
                .add(calculateCostEur(cacheWriteTokens, cacheWritePriceUsd));
    }

    // hind on USD / 1M tokenit -> kulu EUR = tokenid * hind / 1 000 000 * kurss
    private BigDecimal calculateCostEur(Integer tokens, BigDecimal priceUsdPerMillionTokens) {
        if (tokens == null) {
            return null;
        }
        return BigDecimal.valueOf(tokens)
                .multiply(priceUsdPerMillionTokens)
                .multiply(usdToEurRate)
                .divide(TOKENS_PER_PRICE_UNIT, COST_SCALE, RoundingMode.HALF_UP);
    }
}
