package com.school.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false, length = 10)
    private String schoolId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "created_by")
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 30)
    private ActivityApprovalStatus approvalStatus = ActivityApprovalStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_type", nullable = false, length = 50)
    private ActivityVisibilityType visibilityType = ActivityVisibilityType.WHOLE_SCHOOL;

    @Column(name = "cover_media_id")
    private Long coverMediaId;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (approvalStatus == null) approvalStatus = ActivityApprovalStatus.DRAFT;
        if (visibilityType == null) visibilityType = ActivityVisibilityType.WHOLE_SCHOOL;
    }

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId == null ? null : schoolId.trim().toUpperCase(); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public ActivityApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ActivityApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public ActivityVisibilityType getVisibilityType() { return visibilityType; }
    public void setVisibilityType(ActivityVisibilityType visibilityType) { this.visibilityType = visibilityType; }
    public Long getCoverMediaId() { return coverMediaId; }
    public void setCoverMediaId(Long coverMediaId) { this.coverMediaId = coverMediaId; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
