package com.school.attendance.dto;

public class ChangePasswordRequest {

    private String username;
    private String schoolId;
    private String currentPassword;
    private String newPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
