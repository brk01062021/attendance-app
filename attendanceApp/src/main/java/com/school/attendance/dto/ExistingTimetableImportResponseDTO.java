package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class ExistingTimetableImportResponseDTO {
    private String importBatchId;
    private String publishedBatchId;
    private String schoolId;
    private String status;
    private Boolean valid;
    private Boolean canPublish;
    private Integer totalRows;
    private Integer acceptedRows;
    private Integer errorCount;
    private Integer warningCount;
    private Integer conflictsDetected;
    private String message;
    private List<ExistingTimetableImportRowDTO> rows = new ArrayList<>();
    private List<ExistingTimetableImportIssueDTO> issues = new ArrayList<>();
    private List<TimetableConflictDTO> conflicts = new ArrayList<>();
    private List<TimetableEntryDTO> previewEntries = new ArrayList<>();

    public String getImportBatchId() { return importBatchId; }
    public void setImportBatchId(String importBatchId) { this.importBatchId = importBatchId; }
    public String getPublishedBatchId() { return publishedBatchId; }
    public void setPublishedBatchId(String publishedBatchId) { this.publishedBatchId = publishedBatchId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }
    public Boolean getCanPublish() { return canPublish; }
    public void setCanPublish(Boolean canPublish) { this.canPublish = canPublish; }
    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }
    public Integer getAcceptedRows() { return acceptedRows; }
    public void setAcceptedRows(Integer acceptedRows) { this.acceptedRows = acceptedRows; }
    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    public Integer getWarningCount() { return warningCount; }
    public void setWarningCount(Integer warningCount) { this.warningCount = warningCount; }
    public Integer getConflictsDetected() { return conflictsDetected; }
    public void setConflictsDetected(Integer conflictsDetected) { this.conflictsDetected = conflictsDetected; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<ExistingTimetableImportRowDTO> getRows() { return rows; }
    public void setRows(List<ExistingTimetableImportRowDTO> rows) { this.rows = rows == null ? new ArrayList<>() : rows; }
    public List<ExistingTimetableImportIssueDTO> getIssues() { return issues; }
    public void setIssues(List<ExistingTimetableImportIssueDTO> issues) { this.issues = issues == null ? new ArrayList<>() : issues; }
    public List<TimetableConflictDTO> getConflicts() { return conflicts; }
    public void setConflicts(List<TimetableConflictDTO> conflicts) { this.conflicts = conflicts == null ? new ArrayList<>() : conflicts; }
    public List<TimetableEntryDTO> getPreviewEntries() { return previewEntries; }
    public void setPreviewEntries(List<TimetableEntryDTO> previewEntries) { this.previewEntries = previewEntries == null ? new ArrayList<>() : previewEntries; }
}
