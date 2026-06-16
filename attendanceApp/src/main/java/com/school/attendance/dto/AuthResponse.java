package com.school.attendance.dto;

public class AuthResponse {

    private String token;
    private Long userId;
    private Long schoolId; // internal numeric id
    private String schoolCode; // external immutable SaaS tenant id
    private Long teacherId;
    private String teacherName;
    private Long studentId;
    private String studentName;
    private String displayName;
    private String schoolName;
    private String role;
    private boolean forcePasswordChange;
    private boolean credentialsActive = true;

    public AuthResponse(String token,
                        Long userId,
                        Long schoolId,
                        Long teacherId,
                        String teacherName,
                        String displayName,
                        String schoolName,
                        String role) {
        this(token, userId, schoolId, "DEMO", teacherId, teacherName, displayName, schoolName, role, false, true);
    }

    public AuthResponse(String token,
                        Long userId,
                        Long schoolId,
                        String schoolCode,
                        Long teacherId,
                        String teacherName,
                        String displayName,
                        String schoolName,
                        String role) {
        this(token, userId, schoolId, schoolCode, teacherId, teacherName, displayName, schoolName, role, false, true);
    }

    public AuthResponse(String token,
                        Long userId,
                        Long schoolId,
                        String schoolCode,
                        Long teacherId,
                        String teacherName,
                        String displayName,
                        String schoolName,
                        String role,
                        boolean forcePasswordChange) {
        this(token, userId, schoolId, schoolCode, teacherId, teacherName, displayName, schoolName, role, forcePasswordChange, true);
    }

    public AuthResponse(String token,
                        Long userId,
                        Long schoolId,
                        String schoolCode,
                        Long teacherId,
                        String teacherName,
                        String displayName,
                        String schoolName,
                        String role,
                        boolean forcePasswordChange,
                        boolean credentialsActive) {
        this(token, userId, schoolId, schoolCode, teacherId, teacherName, null, null, displayName, schoolName, role, forcePasswordChange, credentialsActive);
    }

    public AuthResponse(String token,
                        Long userId,
                        Long schoolId,
                        String schoolCode,
                        Long teacherId,
                        String teacherName,
                        Long studentId,
                        String studentName,
                        String displayName,
                        String schoolName,
                        String role,
                        boolean forcePasswordChange,
                        boolean credentialsActive) {
        this.token = token;
        this.userId = userId;
        this.schoolId = schoolId;
        this.schoolCode = schoolCode;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.studentId = studentId;
        this.studentName = studentName;
        this.displayName = displayName;
        this.schoolName = schoolName;
        this.role = role;
        this.forcePasswordChange = forcePasswordChange;
        this.credentialsActive = credentialsActive;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public Long getSchoolId() { return schoolId; }
    public String getSchoolCode() { return schoolCode; }
    public String getExternalSchoolId() { return schoolCode; }
    public Long getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getDisplayName() { return displayName; }
    public String getSchoolName() { return schoolName; }
    public String getRole() { return role; }
    public boolean isForcePasswordChange() { return forcePasswordChange; }
    public boolean isCredentialsActive() { return credentialsActive; }
}
