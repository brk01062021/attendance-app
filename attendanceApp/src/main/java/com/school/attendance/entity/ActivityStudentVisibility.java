package com.school.attendance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "activity_student_visibility")
public class ActivityStudentVisibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false, length = 10)
    private String schoolId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "student_username", length = 100)
    private String studentUsername;

    public Long getId() { return id; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId == null ? null : schoolId.trim().toUpperCase(); }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }
}
