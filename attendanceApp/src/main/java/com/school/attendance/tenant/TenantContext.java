package com.school.attendance.tenant;

/**
 * Request-scoped school tenant context.
 * schoolId here means external immutable 4-character school identifier (example: DEMO, BRK1).
 */
public final class TenantContext {

    public static final String DEFAULT_SCHOOL_ID = "DEMO";
    private static final ThreadLocal<String> CURRENT_SCHOOL_ID = new ThreadLocal<>();

    private TenantContext() { }

    public static void setSchoolId(String schoolId) {
        CURRENT_SCHOOL_ID.set(TenantUtils.normalizeOrDefault(schoolId));
    }

    public static String getSchoolId() {
        String schoolId = CURRENT_SCHOOL_ID.get();
        return TenantUtils.normalizeOrDefault(schoolId);
    }

    public static void clear() {
        CURRENT_SCHOOL_ID.remove();
    }
}
