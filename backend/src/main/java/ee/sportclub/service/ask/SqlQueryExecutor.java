package ee.sportclub.service.ask;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// Käivitab valideeritud LLM-i SQL-i võimalikult turvaliselt:
// - read-only transaktsioon (andmeid muuta ei saa ka siis, kui valideerimisest midagi läbi lipsaks)
// - lühike ajalõpp, et raske päring ei koormaks andmebaasi
// - ridade piir, et tulemus ei paisutaks LLM-i sisendit (input tokeneid)
@Component
@RequiredArgsConstructor
public class SqlQueryExecutor {

    public static final int MAX_ROWS = 200;
    private static final String STATEMENT_TIMEOUT = "5s";

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findRowsBy(String validatedSql) {
        jdbcTemplate.execute("SET TRANSACTION READ ONLY");
        jdbcTemplate.execute("SET LOCAL statement_timeout = '" + STATEMENT_TIMEOUT + "'");
        String limitedSql = "SELECT * FROM (" + validatedSql + ") AS llm_query LIMIT " + MAX_ROWS;
        return jdbcTemplate.queryForList(limitedSql);
    }
}
