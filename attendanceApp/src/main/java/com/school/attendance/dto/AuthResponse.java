package com.school.attendance.dto;

public class AuthResponse {

    private String token;
    private Long userId;
    private Long schoolId;
    private Long teacherId;
    private String teacherName;
    private String displayName;
    private String schoolName;
    private String role;

    public AuthResponse(String token,
                        Long userId,
                        Long schoolId,
                        Long teacherId,
                        String teacherName,
                        String displayName,
                        String schoolName,
                        String role) {
        this.token = token;
        this.userId = userId;
        this.schoolId = schoolId;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.displayName = displayName;
        this.schoolName = schoolName;
        this.role = role;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public Long getSchoolId() { return schoolId; }
    public Long getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public String getDisplayName() { return displayName; }
    public String getSchoolName() { return schoolName; }
    public String getRole() { return role; }
}
