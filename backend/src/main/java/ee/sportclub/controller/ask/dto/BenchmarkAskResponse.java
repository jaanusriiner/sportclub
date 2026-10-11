package ee.sportclub.controller.ask.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BenchmarkAskResponse(

        String reasoning,      // LLM-i arutlus enne vastust (FE näitab vastuse peal hiirega)
        String answer,
        Integer inputTokens,   // sisaldab ka cachedTokens
        Integer cachedTokens,  // vahemälust tulnud osa inputTokens'ist (soodsam hind)
        Integer outputTokens,   // sisaldab ka thinkingTokens
        Integer thinkingTokens, // mõtlemiseks kulunud osa outputTokens'ist (arveldatakse output hinnaga)
        BigDecimal inputCost,  // EUR, cached tokenid soodushinnaga
        BigDecimal outputCost,  // EUR
        String generatedSql,    // SQL-režiim: LLM-i koostatud ja käivitatud päring (null = päringut ei tehtud / FULL režiim)
        Integer sqlRowCount     // SQL-režiim: päringu tulemuse ridade arv

) {}
