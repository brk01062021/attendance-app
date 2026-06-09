package com.school.attendance.dto;

public class TimetableBatchSummaryDTO {
    private String batchId;
    private String status;
    private Integer totalEntries;
    private Integer classSections;
    private Integer conflicts;
    private Integer overloadRiskTeachers;
    private Integer completionPercentage;
    private String lastPublishedAt;
    private String approvedBy;
    private String message;
    private String uploadedAt;
    private String uploadedBy;
    private Boolean locked;
    private Boolean latestPublished;
    private Boolean archived;

    public TimetableBatchSummaryDTO() {}

    public TimetableBatchSummaryDTO(String batchId, String status, Integer totalEntries, Integer classSections, Integer conflicts, Integer overloadRiskTeachers, Integer completionPercentage, String lastPublishedAt, String approvedBy, String message) {
        this.batchId = batchId;
        this.status = status;
        this.totalEntries = totalEntries;
        this.classSections = classSections;
        this.conflicts = conflicts;
        this.overloadRiskTeachers = overloadRiskTeachers;
        this.completionPercentage = completionPercentage;
        this.lastPublishedAt = lastPublishedAt;
        this.approvedBy = approvedBy;
        this.message = message;
    }

    public TimetableBatchSummaryDTO(String batchId, String status, Integer totalEntries, Integer classSections, Integer conflicts, Integer overloadRiskTeachers, Integer completionPercentage, String lastPublishedAt, String approvedBy, String message, String uploadedAt, String uploadedBy, Boolean locked, Boolean latestPublished, Boolean archived) {
        this(batchId, status, totalEntries, classSections, conflicts, overloadRiskTeachers, completionPercentage, lastPublishedAt, approvedBy, message);
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
        this.locked = locked;
        this.latestPublished = latestPublished;
        this.archived = archived;
    }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTotalEntries() { return totalEntries; }
    public void setTotalEntries(Integer totalEntries) { this.totalEntries = totalEntries; }
    public Integer getClassSections() { return classSections; }
    public void setClassSections(Integer classSections) { this.classSections = classSections; }
    public Integer getConflicts() { return conflicts; }
    public void setConflicts(Integer conflicts) { this.conflicts = conflicts; }
    public Integer getOverloadRiskTeachers() { return overloadRiskTeachers; }
    public void setOverloadRiskTeachers(Integer overloadRiskTeachers) { this.overloadRiskTeachers = overloadRiskTeachers; }
    public Integer getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
    public String getLastPublishedAt() { return lastPublishedAt; }
    public void setLastPublishedAt(String lastPublishedAt) { this.lastPublishedAt = lastPublishedAt; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }
    public Boolean getLatestPublished() { return latestPublished; }
    public void setLatestPublished(Boolean latestPublished) { this.latestPublished = latestPublished; }
    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }
}
