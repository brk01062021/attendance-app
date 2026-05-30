package com.school.attendance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long teacherId;
    private String teacherName;
    private String username;
    private String password;
    private String role;

    // Internal numeric database school id. Kept for backward compatibility.
    private Long schoolId = 1L;

    // External SaaS tenant id. Immutable 4-character uppercase alphanumeric id.
    @Column(name = "school_code", length = 4, updatable = false)
    private String schoolCode = "DEMO";

    private String displayName;
    private String schoolName = "VidyaSetu Demo School";

    @Column(name = "credentials_active", nullable = false)
    private Boolean credentialsActive = true;

    @Column(name = "force_password_change", nullable = false)
    private Boolean forcePasswordChange = false;

    public Long getId() { return id; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getSchoolId() { return schoolId == null ? 1L : schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public String getSchoolCode() {
        return schoolCode == null || schoolCode.isBlank() ? "DEMO" : schoolCode.toUpperCase();
    }

    public void setSchoolCode(String schoolCode) {
        if (schoolCode == null || schoolCode.isBlank()) {
            this.schoolCode = "DEMO";
        } else {
            this.schoolCode = schoolCode.trim().toUpperCase();
        }
    }

    public String getDisplayName() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        if (teacherName != null && !teacherName.isBlank()) {
            return teacherName;
        }
        return username;
    }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getSchoolName() {
        return schoolName == null || schoolName.isBlank() ? "VidyaSetu Demo School" : schoolName;
    }

    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public Boolean getCredentialsActive() { return credentialsActive == null ? true : credentialsActive; }
    public void setCredentialsActive(Boolean credentialsActive) { this.credentialsActive = credentialsActive == null ? true : credentialsActive; }

    public Boolean getForcePasswordChange() { return forcePasswordChange == null ? false : forcePasswordChange; }
    public void setForcePasswordChange(Boolean forcePasswordChange) { this.forcePasswordChange = forcePasswordChange == null ? false : forcePasswordChange; }
}
