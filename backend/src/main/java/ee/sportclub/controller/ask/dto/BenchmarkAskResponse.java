package ee.sportclub.controller.ask.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonPropertyOrder({"reasoning", "answer", "inputTokens","cachedTokens","outputTokens", "inputCost", "outputCost"})
public record BenchmarkAskResponse(

        String reasoning,      // LLM-i arutlus enne vastust (FE näitab vastuse peal hiirega)
        String answer,
        Integer inputTokens,   // sisaldab ka cachedTokens
        Integer cachedTokens,  // vahemälust tulnud osa inputTokens'ist (soodsam hind)
        Integer outputTokens,
        BigDecimal inputCost,  // EUR, cached tokenid soodushinnaga
        BigDecimal outputCost  // EUR

) {}
