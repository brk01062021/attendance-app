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
    private Long schoolId = 1L;
    private String displayName;
    private String schoolName = "VidyaSetu Demo School";

    public Long getId() {
        return id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getSchoolId() {
        return schoolId == null ? 1L : schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
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

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSchoolName() {
        return schoolName == null || schoolName.isBlank() ? "VidyaSetu Demo School" : schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}