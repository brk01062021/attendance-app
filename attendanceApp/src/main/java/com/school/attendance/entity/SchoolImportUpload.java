package com.school.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_import_uploads", indexes = {
        @Index(name = "idx_school_import_upload_school", columnList = "schoolCode"),
        @Index(name = "idx_school_import_upload_checksum", columnList = "checksum")
})
public class SchoolImportUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 4, nullable = false)
    private String schoolCode;
    private String academicYear;
    private String importType;
    private String fileName;
    private String checksum;
    private String status;
    private String importBatchId;
    private int totalRows;
    private int totalSheets;
    private int errorCount;
    private int warningCount;
    private boolean committed;
    private boolean rolledBack;
    private String uploadedByRole;
    private LocalDateTime uploadedAt = LocalDateTime.now();
    private LocalDateTime committedAt;
    private LocalDateTime rolledBackAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String previewJson;

    public Long getId() { return id; }
    public String getSchoolCode() { return schoolCode; }
    public void setSchoolCode(String schoolCode) { this.schoolCode = schoolCode; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImportBatchId() { return importBatchId; }
    public void setImportBatchId(String importBatchId) { this.importBatchId = importBatchId; }
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
    public String getUploadedByRole() { return uploadedByRole; }
    public void setUploadedByRole(String uploadedByRole) { this.uploadedByRole = uploadedByRole; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public LocalDateTime getCommittedAt() { return committedAt; }
    public void setCommittedAt(LocalDateTime committedAt) { this.committedAt = committedAt; }
    public LocalDateTime getRolledBackAt() { return rolledBackAt; }
    public void setRolledBackAt(LocalDateTime rolledBackAt) { this.rolledBackAt = rolledBackAt; }
    public String getPreviewJson() { return previewJson; }
    public void setPreviewJson(String previewJson) { this.previewJson = previewJson; }
}
