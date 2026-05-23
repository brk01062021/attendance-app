package com.school.attendance.dto.imports;

import java.util.ArrayList;
import java.util.List;

public class ImportValidationRequestDTO {
    private String schoolId;
    private String importType;
    private String fileName;
    private String requestedByRole;
    private List<ImportSheetPreviewDTO> sheets = new ArrayList<>();

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getRequestedByRole() { return requestedByRole; }
    public void setRequestedByRole(String requestedByRole) { this.requestedByRole = requestedByRole; }
    public List<ImportSheetPreviewDTO> getSheets() { return sheets; }
    public void setSheets(List<ImportSheetPreviewDTO> sheets) { this.sheets = sheets; }
}
