package com.school.attendance.dto;

public class TimetableVersionDTO {
    private Integer versionNumber;
    private String batchId;
    private String createdAt;
    private String createdBy;
    private String changeType;
    private Integer entriesCount;
    private String notes;

    public TimetableVersionDTO() {}
    public TimetableVersionDTO(Integer versionNumber, String batchId, String createdAt, String createdBy, String changeType, Integer entriesCount, String notes) {
        this.versionNumber = versionNumber;
        this.batchId = batchId;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.changeType = changeType;
        this.entriesCount = entriesCount;
        this.notes = notes;
    }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public Integer getEntriesCount() { return entriesCount; }
    public void setEntriesCount(Integer entriesCount) { this.entriesCount = entriesCount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
