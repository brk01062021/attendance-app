package com.school.attendance.dto;

public class TimetablePublishAuditDTO {
    private String auditId;
    private String batchId;
    private String status;
    private String publishedAt;
    private String approvedBy;
    private Integer publishedEntries;
    private Integer remainingConflicts;
    private Integer classSections;
    private String message;
    private String previousActiveBatchId;
    private String newActiveBatchId;

    public TimetablePublishAuditDTO() {}

    public TimetablePublishAuditDTO(String auditId, String batchId, String status, String publishedAt, String approvedBy, Integer publishedEntries, Integer remainingConflicts, Integer classSections, String message) {
        this.auditId = auditId;
        this.batchId = batchId;
        this.status = status;
        this.publishedAt = publishedAt;
        this.approvedBy = approvedBy;
        this.publishedEntries = publishedEntries;
        this.remainingConflicts = remainingConflicts;
        this.classSections = classSections;
        this.message = message;
    }

    public TimetablePublishAuditDTO(String auditId, String batchId, String status, String publishedAt, String approvedBy, Integer publishedEntries, Integer remainingConflicts, Integer classSections, String message, String previousActiveBatchId, String newActiveBatchId) {
        this(auditId, batchId, status, publishedAt, approvedBy, publishedEntries, remainingConflicts, classSections, message);
        this.previousActiveBatchId = previousActiveBatchId;
        this.newActiveBatchId = newActiveBatchId;
    }

    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public Integer getPublishedEntries() { return publishedEntries; }
    public void setPublishedEntries(Integer publishedEntries) { this.publishedEntries = publishedEntries; }
    public Integer getRemainingConflicts() { return remainingConflicts; }
    public void setRemainingConflicts(Integer remainingConflicts) { this.remainingConflicts = remainingConflicts; }
    public Integer getClassSections() { return classSections; }
    public void setClassSections(Integer classSections) { this.classSections = classSections; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPreviousActiveBatchId() { return previousActiveBatchId; }
    public void setPreviousActiveBatchId(String previousActiveBatchId) { this.previousActiveBatchId = previousActiveBatchId; }
    public String getNewActiveBatchId() { return newActiveBatchId; }
    public void setNewActiveBatchId(String newActiveBatchId) { this.newActiveBatchId = newActiveBatchId; }
}
