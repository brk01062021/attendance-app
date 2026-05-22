package com.school.attendance.dto;

public class LoginRequest {

    private String username;
    private String password;
    private String schoolId; // external 4-char tenant id from login screen

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
}
