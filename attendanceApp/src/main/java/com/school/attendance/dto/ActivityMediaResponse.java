package com.school.attendance.dto;

import com.school.attendance.entity.ActivityMedia;

import java.time.LocalDateTime;

public class ActivityMediaResponse {
    private Long id;
    private Long activityId;
    private String schoolId;
    private String mediaType;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String storageKey;
    private String thumbnailKey;
    private Integer displayOrder;
    private Long uploadedBy;
    private LocalDateTime uploadedAt;
    private String url;
    private String thumbnailUrl;

    public static ActivityMediaResponse from(ActivityMedia media) {
        ActivityMediaResponse response = new ActivityMediaResponse();
        response.id = media.getId();
        response.activityId = media.getActivityId();
        response.schoolId = media.getSchoolId();
        response.mediaType = media.getMediaType() == null ? null : media.getMediaType().name();
        response.fileName = media.getFileName();
        response.contentType = media.getContentType();
        response.fileSize = media.getFileSize();
        response.storageKey = media.getStorageKey();
        response.thumbnailKey = media.getThumbnailKey();
        response.displayOrder = media.getDisplayOrder();
        response.uploadedBy = media.getUploadedBy();
        response.uploadedAt = media.getUploadedAt();
        response.url = String.format("/api/activities/%d/media/%d/content", media.getActivityId(), media.getId());
        response.thumbnailUrl = response.url;
        return response;
    }

    public Long getId() { return id; }
    public Long getActivityId() { return activityId; }
    public String getSchoolId() { return schoolId; }
    public String getMediaType() { return mediaType; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
    public String getStorageKey() { return storageKey; }
    public String getThumbnailKey() { return thumbnailKey; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Long getUploadedBy() { return uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getUrl() { return url; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}
