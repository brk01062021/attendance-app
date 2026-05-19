package com.school.attendance.dto;

public class TimetablePublishResponseDTO {
    private Boolean success;
    private String batchId;
    private String status;
    private String message;
    private Integer publishedEntries;
    private Integer remainingConflicts;
    private String publishedAt;
    private String approvedBy;
    private String notificationMessage;

    public TimetablePublishResponseDTO() {}
    public TimetablePublishResponseDTO(Boolean success, String batchId, String status, String message, Integer publishedEntries, Integer remainingConflicts) {
        this(success, batchId, status, message, publishedEntries, remainingConflicts, null, null, null);
    }
    public TimetablePublishResponseDTO(Boolean success, String batchId, String status, String message, Integer publishedEntries, Integer remainingConflicts, String publishedAt, String approvedBy, String notificationMessage) {
        this.success = success;
        this.batchId = batchId;
        this.status = status;
        this.message = message;
        this.publishedEntries = publishedEntries;
        this.remainingConflicts = remainingConflicts;
        this.publishedAt = publishedAt;
        this.approvedBy = approvedBy;
        this.notificationMessage = notificationMessage;
    }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Integer getPublishedEntries() { return publishedEntries; }
    public void setPublishedEntries(Integer publishedEntries) { this.publishedEntries = publishedEntries; }
    public Integer getRemainingConflicts() { return remainingConflicts; }
    public void setRemainingConflicts(Integer remainingConflicts) { this.remainingConflicts = remainingConflicts; }
    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getNotificationMessage() { return notificationMessage; }
    public void setNotificationMessage(String notificationMessage) { this.notificationMessage = notificationMessage; }
}
