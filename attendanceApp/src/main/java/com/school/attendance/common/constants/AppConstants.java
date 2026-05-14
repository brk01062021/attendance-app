package com.school.attendance.common.constants;

public class AppConstants {

    private AppConstants() {
    }

    // =========================
    // ROLES
    // =========================

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_PARENT = "PARENT";
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_PRINCIPAL = "PRINCIPAL";
    public static final String ROLE_ALL = "ALL";

    // =========================
    // ATTENDANCE STATUS
    // =========================

    public static final String PRESENT = "PRESENT";
    public static final String ABSENT = "ABSENT";
    public static final String LATE = "LATE";
    public static final String HALF_DAY = "HALF_DAY";
    public static final String NOT_MARKED = "NOT_MARKED";

    // =========================
    // LEAVE TYPES
    // =========================

    public static final String PLANNED = "PLANNED";
    public static final String UNPLANNED = "UNPLANNED";

    // =========================
    // REPORT TYPES
    // =========================

    public static final String DAILY = "DAILY";
    public static final String WEEKLY = "WEEKLY";
    public static final String MONTHLY = "MONTHLY";

    // =========================
    // ANALYTICS TYPES
    // =========================

    public static final String CLASS_WISE = "CLASS_WISE";
    public static final String SECTION_WISE = "SECTION_WISE";
    public static final String SUBJECT_WISE = "SUBJECT_WISE";

    // =========================
    // REPLACEMENT STATUS / NOTIFICATION TYPES
    // =========================

    public static final String REPLACEMENT_ASSIGNED = "ASSIGNED";
    public static final String REPLACEMENT_PENDING = "PENDING";
    public static final String REPLACEMENT_STATUS_ASSIGNED = "REPLACEMENT_ASSIGNED";
    public static final String REPLACEMENT_STATUS_PENDING = "REPLACEMENT_PENDING";
    public static final String NOTIFICATION_REPLACEMENT_ASSIGNED = "REPLACEMENT_ASSIGNED";
    public static final String NOTIFICATION_ATTENDANCE_REPORT = "ATTENDANCE_REPORT";

    // =========================
    // COMMON MESSAGES
    // =========================

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String DATA_NOT_FOUND = "Data not found";
}
