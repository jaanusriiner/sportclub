package ee.sportclub.service.ask;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// LLM-i koostatud SQL-i kontroll enne käivitamist. Lubatud on ainult üks SELECT vaatest v_training_date_extended.
// Kontroll on esimene kaitseliin; teine on SqlQueryExecutor (read-only transaktsioon, ajalõpp, ridade piir).
// Vea teade läheb LLM-ile tagasi parandamiseks, seega on see inglise keeles ja konkreetne.
@Component
public class SqlQueryValidator {

    public static final String ALLOWED_VIEW = "v_training_date_extended";

    private static final Pattern FORBIDDEN_KEYWORDS = Pattern.compile(
            "\\b(INSERT|UPDATE|DELETE|MERGE|DROP|TRUNCATE|ALTER|CREATE|GRANT|REVOKE|COPY|CALL|DO|EXECUTE|PREPARE"
                    + "|LISTEN|NOTIFY|VACUUM|SET|RESET|LOCK|INTO)\\b");
    // andmebaasi süsteemifunktsioonid (nt pg_sleep, pg_read_file) ja välisühendused
    private static final Pattern FORBIDDEN_FUNCTIONS = Pattern.compile("\\b(PG_\\w+|DBLINK\\w*|LO_\\w+|CURRENT_SETTING)\\b");
    private static final Pattern TABLE_REFERENCE = Pattern.compile("\\b(?:FROM|JOIN)\\s+([A-Z_][A-Z0-9_.\"]*)");
    // funktsioonid, mille süntaksis on sõna FROM, mis pole tabeliviide (nt EXTRACT(DOW FROM training_date))
    private static final Pattern FROM_INSIDE_FUNCTION = Pattern.compile(
            "\\b(?:EXTRACT|SUBSTRING|TRIM|OVERLAY)\\s*\\([^()]*\\)|\\bDISTINCT\\s+FROM\\b");
    private static final String FORBIDDEN_COLUMN = "TRAINER_EMAIL";

    // tagastab puhastatud SQL-i (ilma lõpu semikoolonita), mida on ohutu alampäringuna mähkida
    public String getValidatedSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL is empty");
        }
        String cleanedSql = removeTrailingSemicolon(sql.strip());
        String upperCaseSql = cleanedSql.toUpperCase(Locale.ROOT);

        if (!upperCaseSql.startsWith("SELECT")) {
            throw new IllegalArgumentException("Only a SELECT statement is allowed");
        }
        if (cleanedSql.contains(";")) {
            throw new IllegalArgumentException("Only one SQL statement is allowed");
        }
        if (cleanedSql.contains("--") || cleanedSql.contains("/*")) {
            throw new IllegalArgumentException("SQL comments are not allowed");
        }
        if (FORBIDDEN_KEYWORDS.matcher(upperCaseSql).find()) {
            throw new IllegalArgumentException("SQL contains a forbidden keyword - only reading data is allowed");
        }
        if (FORBIDDEN_FUNCTIONS.matcher(upperCaseSql).find()) {
            throw new IllegalArgumentException("SQL contains a forbidden database function");
        }
        if (upperCaseSql.contains(FORBIDDEN_COLUMN)) {
            throw new IllegalArgumentException("Column trainer_email must not be queried");
        }
        validateTableReferences(upperCaseSql);
        return cleanedSql;
    }

    // FROM/JOIN järel tohib olla ainult lubatud vaade (alampäring "FROM (" ei satu mustrisse)
    private void validateTableReferences(String upperCaseSql) {
        String sqlWithoutFromFunctions = FROM_INSIDE_FUNCTION.matcher(upperCaseSql).replaceAll(" ");
        Matcher matcher = TABLE_REFERENCE.matcher(sqlWithoutFromFunctions);
        boolean hasTableReference = false;
        while (matcher.find()) {
            hasTableReference = true;
            String tableName = matcher.group(1).replace("\"", "");
            String tableNameWithoutSchema = tableName.substring(tableName.lastIndexOf('.') + 1);
            if (!tableNameWithoutSchema.equals(ALLOWED_VIEW.toUpperCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Only the view " + ALLOWED_VIEW + " may be queried, found: "
                        + tableName.toLowerCase(Locale.ROOT));
            }
        }
        if (!hasTableReference) {
            throw new IllegalArgumentException("SQL must select from " + ALLOWED_VIEW);
        }
    }

    private String removeTrailingSemicolon(String sql) {
        return sql.endsWith(";") ? sql.substring(0, sql.length() - 1).strip() : sql;
    }
}
