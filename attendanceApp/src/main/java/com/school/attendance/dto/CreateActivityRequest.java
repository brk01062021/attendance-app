package com.school.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public class CreateActivityRequest {
    private String title;
    private String description;
    private LocalDate activityDate;
    private String visibilityType;
    private Long createdBy;
    private List<String> classNames;
    private List<Long> studentIds;
    private List<String> studentUsernames;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }
    public String getVisibilityType() { return visibilityType; }
    public void setVisibilityType(String visibilityType) { this.visibilityType = visibilityType; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public List<String> getClassNames() { return classNames; }
    public void setClassNames(List<String> classNames) { this.classNames = classNames; }
    public List<Long> getStudentIds() { return studentIds; }
    public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }
    public List<String> getStudentUsernames() { return studentUsernames; }
    public void setStudentUsernames(List<String> studentUsernames) { this.studentUsernames = studentUsernames; }
}
