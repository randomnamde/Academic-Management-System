package com.student.util;

public enum ExportFormat {
    CSV,
    XLSX;

    public static ExportFormat fromNullable(String raw) {
        if (raw == null || raw.isBlank()) {
            return CSV;
        }
        return "xlsx".equalsIgnoreCase(raw) ? XLSX : CSV;
    }
}
