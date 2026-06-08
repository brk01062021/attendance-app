package com.school.attendance.storage;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

final class StorageKeyFactory {
    private StorageKeyFactory() {
    }

    static String buildKey(String environment, String schoolId, String module, String originalFilename) {
        LocalDate today = LocalDate.now();
        String safeEnv = sanitizeSegment(defaultValue(environment, "local")).toLowerCase(Locale.ROOT);
        String safeSchoolId = sanitizeSegment(defaultValue(schoolId, "UNKNOWN")).toUpperCase(Locale.ROOT);
        String safeModule = sanitizeSegment(defaultValue(module, "general-upload")).toLowerCase(Locale.ROOT);
        String safeFilename = sanitizeFilename(defaultValue(originalFilename, "upload.xlsx"));
        return String.format("%s/%s/%s/%04d/%02d/%s_%s",
                safeEnv,
                safeSchoolId,
                safeModule,
                today.getYear(),
                today.getMonthValue(),
                UUID.randomUUID(),
                safeFilename);
    }

    private static String sanitizeSegment(String value) {
        String sanitized = value.trim().replaceAll("[^A-Za-z0-9_-]", "-").replaceAll("-+", "-");
        return sanitized.isBlank() ? "unknown" : sanitized;
    }

    private static String sanitizeFilename(String value) {
        String sanitized = value.trim().replace('\\', '_').replace('/', '_').replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.isBlank() ? "upload.xlsx" : sanitized;
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
