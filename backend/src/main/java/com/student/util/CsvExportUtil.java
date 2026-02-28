package com.student.util;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvExportUtil {

    private CsvExportUtil() {
    }

    public static byte[] write(List<String> headers, List<List<String>> rows) {
        StringBuilder builder = new StringBuilder();
        // UTF-8 BOM to keep Chinese readable in Excel.
        builder.append('\uFEFF');
        builder.append(toCsvLine(headers));
        builder.append('\n');
        for (List<String> row : rows) {
            builder.append(toCsvLine(row));
            builder.append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String toCsvLine(List<String> cells) {
        return cells.stream()
                .map(CsvExportUtil::escapeCell)
                .collect(Collectors.joining(","));
    }

    private static String escapeCell(String raw) {
        String value = raw == null ? "" : raw;
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
