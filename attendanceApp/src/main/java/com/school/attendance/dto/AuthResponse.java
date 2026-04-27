package com.school.attendance.dto;

public class AuthResponse {

    private String token;
    private Long teacherId;
    private String teacherName;

    public AuthResponse(String token, Long teacherId, String teacherName) {
        this.token = token;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
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
}