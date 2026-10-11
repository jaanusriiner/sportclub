package ee.sportclub.infrastructure.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Andmebaasi päringu tulemus (JdbcTemplate.queryForList) kompaktseks CSV-ks LLM-i promptis:
// päis veerunimedest, iga rida eraldi real, eraldaja semikoolon (aadressides on komasid).
public final class CsvFormatter {

    public static final String CSV_SEPARATOR = ";";

    private CsvFormatter() {
    }

    // null -> tühi väli; tühja tulemuse korral tühi string
    public static String toCsv(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "";
        }
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(CSV_SEPARATOR, rows.getFirst().keySet())).append('\n');
        for (Map<String, Object> row : rows) {
            List<String> fields = new ArrayList<>();
            for (Object value : row.values()) {
                fields.add(toCsvField(value));
            }
            csv.append(String.join(CSV_SEPARATOR, fields)).append('\n');
        }
        return csv.toString();
    }

    // eraldaja, jutumärgi või reavahetusega väärtus pannakse jutumärkidesse (jutumärgid kahekordistatakse)
    private static String toCsvField(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(CSV_SEPARATOR) || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
