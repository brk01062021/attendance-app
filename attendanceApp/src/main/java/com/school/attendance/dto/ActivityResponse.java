package com.school.attendance.dto;

import com.school.attendance.entity.Activity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ActivityResponse {
    private Long id;
    private String schoolId;
    private String title;
    private String description;
    private LocalDate activityDate;
    private Long createdBy;
    private String approvalStatus;
    private String visibilityType;
    private Long coverMediaId;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ActivityResponse from(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.id = activity.getId();
        response.schoolId = activity.getSchoolId();
        response.title = activity.getTitle();
        response.description = activity.getDescription();
        response.activityDate = activity.getActivityDate();
        response.createdBy = activity.getCreatedBy();
        response.approvalStatus = activity.getApprovalStatus() == null ? null : activity.getApprovalStatus().name();
        response.visibilityType = activity.getVisibilityType() == null ? null : activity.getVisibilityType().name();
        response.coverMediaId = activity.getCoverMediaId();
        response.publishedAt = activity.getPublishedAt();
        response.createdAt = activity.getCreatedAt();
        response.updatedAt = activity.getUpdatedAt();
        return response;
    }

    public Long getId() { return id; }
    public String getSchoolId() { return schoolId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getActivityDate() { return activityDate; }
    public Long getCreatedBy() { return createdBy; }
    public String getApprovalStatus() { return approvalStatus; }
    public String getVisibilityType() { return visibilityType; }
    public Long getCoverMediaId() { return coverMediaId; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
