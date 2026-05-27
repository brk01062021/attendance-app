package com.school.attendance.dto.imports;

import java.time.LocalDateTime;

public class ImportUploadResponseDTO {
    private Long uploadId;
    private String schoolId;
    private String academicYear;
    private String importType;
    private String fileName;
    private String checksum;
    private String status;
    private boolean duplicateFile;
    private ImportPreviewResponseDTO preview;
    private LocalDateTime uploadedAt;

    public Long getUploadId() { return uploadId; }
    public void setUploadId(Long uploadId) { this.uploadId = uploadId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
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
    public boolean isDuplicateFile() { return duplicateFile; }
    public void setDuplicateFile(boolean duplicateFile) { this.duplicateFile = duplicateFile; }
    public ImportPreviewResponseDTO getPreview() { return preview; }
    public void setPreview(ImportPreviewResponseDTO preview) { this.preview = preview; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
