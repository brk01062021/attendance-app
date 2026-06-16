package com.school.attendance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "activity_class_visibility")
public class ActivityClassVisibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false, length = 10)
    private String schoolId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(length = 50)
    private String section;

    public Long getId() { return id; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId == null ? null : schoolId.trim().toUpperCase(); }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
}
