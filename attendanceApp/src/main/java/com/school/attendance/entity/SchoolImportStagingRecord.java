package com.school.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_import_staging_records", indexes = {
        @Index(name = "idx_import_staging_upload", columnList = "uploadId"),
        @Index(name = "idx_import_staging_school_batch", columnList = "schoolCode,importBatchId"),
        @Index(name = "idx_import_staging_sheet", columnList = "sheetName")
})
public class SchoolImportStagingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long uploadId;

    @Column(length = 4, nullable = false)
    private String schoolCode;

    private String academicYear;
    private String importBatchId;
    private String sheetName;
    private int workbookRowNumber;
    private String recordKey;
    private String status = "STAGED";
    private LocalDateTime stagedAt = LocalDateTime.now();
    private LocalDateTime rolledBackAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rowJson;

    public Long getId() { return id; }
    public Long getUploadId() { return uploadId; }
    public void setUploadId(Long uploadId) { this.uploadId = uploadId; }
    public String getSchoolCode() { return schoolCode; }
    public void setSchoolCode(String schoolCode) { this.schoolCode = schoolCode; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getImportBatchId() { return importBatchId; }
    public void setImportBatchId(String importBatchId) { this.importBatchId = importBatchId; }
    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    public int getWorkbookRowNumber() { return workbookRowNumber; }
    public void setWorkbookRowNumber(int workbookRowNumber) { this.workbookRowNumber = workbookRowNumber; }
    public String getRecordKey() { return recordKey; }
    public void setRecordKey(String recordKey) { this.recordKey = recordKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStagedAt() { return stagedAt; }
    public void setStagedAt(LocalDateTime stagedAt) { this.stagedAt = stagedAt; }
    public LocalDateTime getRolledBackAt() { return rolledBackAt; }
    public void setRolledBackAt(LocalDateTime rolledBackAt) { this.rolledBackAt = rolledBackAt; }
    public String getRowJson() { return rowJson; }
    public void setRowJson(String rowJson) { this.rowJson = rowJson; }
}
