package com.school.attendance.dto;

public class TimetableNotificationDTO {
    private String notificationId;
    private String batchId;
    private String audience;
    private String title;
    private String message;
    private String createdAt;

    public TimetableNotificationDTO() {}
    public TimetableNotificationDTO(String notificationId, String batchId, String audience, String title, String message, String createdAt) {
        this.notificationId = notificationId;
        this.batchId = batchId;
        this.audience = audience;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
    }
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
