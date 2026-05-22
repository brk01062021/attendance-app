package com.school.attendance.dto.imports;

public class ImportPreviewRequestDTO {
    private String schoolId;
    private String importType;
    private String fileName;

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
