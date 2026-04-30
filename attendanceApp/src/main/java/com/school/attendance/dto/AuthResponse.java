package com.school.attendance.dto;

public class AuthResponse {

    private String token;
    private Long teacherId;
    private String teacherName;
    private String role;

    public AuthResponse(String token,
                        Long teacherId,
                        String teacherName,
                        String role) {
        this.token = token;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getRole() {
        return role;
    }
}