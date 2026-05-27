package com.school.attendance.dto.imports;

import java.time.LocalDateTime;

public class ImportUploadHistoryDTO {
    private Long uploadId;
    private String schoolId;
    private String fileName;
    private String importType;
    private String academicYear;
    private String status;
    private int totalRows;
    private int totalSheets;
    private int errorCount;
    private int warningCount;
    private boolean committed;
    private boolean rolledBack;
    private LocalDateTime uploadedAt;

    public Long getUploadId() { return uploadId; }
    public void setUploadId(Long uploadId) { this.uploadId = uploadId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getTotalSheets() { return totalSheets; }
    public void setTotalSheets(int totalSheets) { this.totalSheets = totalSheets; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    public int getWarningCount() { return warningCount; }
    public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
    public boolean isCommitted() { return committed; }
    public void setCommitted(boolean committed) { this.committed = committed; }
    public boolean isRolledBack() { return rolledBack; }
    public void setRolledBack(boolean rolledBack) { this.rolledBack = rolledBack; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
