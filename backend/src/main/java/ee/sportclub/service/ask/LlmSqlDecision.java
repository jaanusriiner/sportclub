package ee.sportclub.service.ask;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// SQL-režiimi 1. sammu (päringu otsus) LLM-i struktureeritud väljund.
// sql = null tähendab, et uut päringut pole vaja: küsimusele saab vastata kataloogist või varasematest tulemustest.
@JsonPropertyOrder({"reasoning", "sql"})
public record LlmSqlDecision(

        @JsonPropertyDescription("Step-by-step reasoning: which data is needed and whether a new query is required. Fill this first.")
        String reasoning,

        @JsonPropertyDescription("One PostgreSQL SELECT statement against v_training_date_extended, or null if no new query is needed.")
        String sql

) {}
