package ee.sportclub.controller.ask.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BenchmarkAskResponse(

        String answer,
        Integer inputTokens,   // sisaldab ka cachedTokens
        Integer cachedTokens,  // vahemälust tulnud osa inputTokens'ist (soodsam hind)
        Integer outputTokens,
        BigDecimal inputCost,  // EUR, cached tokenid soodushinnaga
        BigDecimal outputCost  // EUR

) {}
