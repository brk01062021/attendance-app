package com.school.attendance.dto;

import java.time.LocalDate;

public class UpdateActivityRequest {
    private String title;
    private String description;
    private LocalDate activityDate;
    private String visibilityType;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }
    public String getVisibilityType() { return visibilityType; }
    public void setVisibilityType(String visibilityType) { this.visibilityType = visibilityType; }
}
