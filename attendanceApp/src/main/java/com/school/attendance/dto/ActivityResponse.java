package com.school.attendance.dto;

import com.school.attendance.entity.Activity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Integer mediaCount = 0;
    private Integer photoCount = 0;
    private Integer videoCount = 0;
    private List<ActivityMediaResponse> mediaList = new ArrayList<>();
    private List<ActivityMediaResponse> media = new ArrayList<>();
    private List<ActivityMediaResponse> mediaItems = new ArrayList<>();

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

    public static ActivityResponse from(Activity activity, List<ActivityMediaResponse> mediaList) {
        ActivityResponse response = from(activity);
        List<ActivityMediaResponse> safeMedia = mediaList == null ? List.of() : mediaList;
        response.mediaList = safeMedia;
        response.media = safeMedia;
        response.mediaItems = safeMedia;
        response.mediaCount = safeMedia.size();
        response.photoCount = (int) safeMedia.stream().filter(item -> "PHOTO".equalsIgnoreCase(item.getMediaType())).count();
        response.videoCount = (int) safeMedia.stream().filter(item -> "VIDEO".equalsIgnoreCase(item.getMediaType())).count();
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
    public Integer getMediaCount() { return mediaCount; }
    public Integer getPhotoCount() { return photoCount; }
    public Integer getVideoCount() { return videoCount; }
    public List<ActivityMediaResponse> getMediaList() { return mediaList; }
    public List<ActivityMediaResponse> getMedia() { return media; }
    public List<ActivityMediaResponse> getMediaItems() { return mediaItems; }
}

