package com.school.attendance.tenant;

import java.util.Locale;
import java.util.regex.Pattern;

public final class TenantUtils {

    public static final int SCHOOL_ID_LENGTH = 4;
    private static final Pattern SCHOOL_ID_PATTERN = Pattern.compile("^[A-Z0-9]{4}$");

    private TenantUtils() { }

    public static boolean isValidSchoolId(String schoolId) {
        return schoolId != null && SCHOOL_ID_PATTERN.matcher(schoolId.trim().toUpperCase(Locale.ROOT)).matches();
    }

    public static String normalize(String schoolId) {
        if (schoolId == null) {
            return null;
        }
        return schoolId.trim().toUpperCase(Locale.ROOT);
    }

    public static String normalizeOrDefault(String schoolId) {
        String normalized = normalize(schoolId);
        return isValidSchoolId(normalized) ? normalized : TenantContext.DEFAULT_SCHOOL_ID;
    }

    public static String requireValidSchoolId(String schoolId) {
        String normalized = normalize(schoolId);
        if (!isValidSchoolId(normalized)) {
            throw new IllegalArgumentException("school_id must be exactly 4 uppercase alphanumeric characters");
        }
        return normalized;
    }
}
