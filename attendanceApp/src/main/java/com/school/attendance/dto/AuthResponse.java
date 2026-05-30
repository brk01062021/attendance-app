package com.school.attendance.dto;

public class AuthResponse {

    private String token;
    private Long userId;
    private Long schoolId; // internal numeric id
    private String schoolCode; // external immutable SaaS tenant id
    private Long teacherId;
    private String teacherName;
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
        this(token, userId, schoolId, "DEMO", teacherId, teacherName, displayName, schoolName, role, false);
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
        this(token, userId, schoolId, schoolCode, teacherId, teacherName, displayName, schoolName, role, false);
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
        this.token = token;
        this.userId = userId;
        this.schoolId = schoolId;
        this.schoolCode = schoolCode;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.displayName = displayName;
        this.schoolName = schoolName;
        this.role = role;
        this.forcePasswordChange = forcePasswordChange;
        this.credentialsActive = true;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public Long getSchoolId() { return schoolId; }
    public String getSchoolCode() { return schoolCode; }
    public String getExternalSchoolId() { return schoolCode; }
    public Long getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public String getDisplayName() { return displayName; }
    public String getSchoolName() { return schoolName; }
    public String getRole() { return role; }
    public boolean isForcePasswordChange() { return forcePasswordChange; }
    public boolean isCredentialsActive() { return credentialsActive; }
    public void setCredentialsActive(boolean credentialsActive) { this.credentialsActive = credentialsActive; }
}


