package com.school.attendance.dto;

public class TimetableArchiveSummaryDTO {
    private String batchId;
    private String archivedAt;
    private String archivedBy;
    private Integer entriesCount;
    private String status;
    private String message;

    public TimetableArchiveSummaryDTO() {}
    public TimetableArchiveSummaryDTO(String batchId, String archivedAt, String archivedBy, Integer entriesCount, String status, String message) {
        this.batchId = batchId;
        this.archivedAt = archivedAt;
        this.archivedBy = archivedBy;
        this.entriesCount = entriesCount;
        this.status = status;
        this.message = message;
    }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getArchivedAt() { return archivedAt; }
    public void setArchivedAt(String archivedAt) { this.archivedAt = archivedAt; }
    public String getArchivedBy() { return archivedBy; }
    public void setArchivedBy(String archivedBy) { this.archivedBy = archivedBy; }
    public Integer getEntriesCount() { return entriesCount; }
    public void setEntriesCount(Integer entriesCount) { this.entriesCount = entriesCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
