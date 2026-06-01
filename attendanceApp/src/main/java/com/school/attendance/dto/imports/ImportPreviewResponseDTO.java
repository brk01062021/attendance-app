package com.school.attendance.dto.imports;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImportPreviewResponseDTO {
    private String schoolId;
    private String importType;
    private String fileName;
    private boolean valid;
    private boolean tenantSafe;
    private String status;
    private String summary;
    private Map<String, Integer> rowCounts = new LinkedHashMap<>();
    private List<ImportSheetPreviewDTO> previewSheets = new ArrayList<>();
    private List<ImportValidationIssueDTO> issues = new ArrayList<>();
    private WorkbookErrorIntelligenceDTO errorIntelligence;
    private Instant previewedAt = Instant.now();

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public boolean isTenantSafe() { return tenantSafe; }
    public void setTenantSafe(boolean tenantSafe) { this.tenantSafe = tenantSafe; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Map<String, Integer> getRowCounts() { return rowCounts; }
    public void setRowCounts(Map<String, Integer> rowCounts) { this.rowCounts = rowCounts; }
    public List<ImportSheetPreviewDTO> getPreviewSheets() { return previewSheets; }
    public void setPreviewSheets(List<ImportSheetPreviewDTO> previewSheets) { this.previewSheets = previewSheets; }
    public List<ImportValidationIssueDTO> getIssues() { return issues; }
    public void setIssues(List<ImportValidationIssueDTO> issues) { this.issues = issues; }
    public WorkbookErrorIntelligenceDTO getErrorIntelligence() { return errorIntelligence; }
    public void setErrorIntelligence(WorkbookErrorIntelligenceDTO errorIntelligence) { this.errorIntelligence = errorIntelligence; }
    public Instant getPreviewedAt() { return previewedAt; }
    public void setPreviewedAt(Instant previewedAt) { this.previewedAt = previewedAt; }
}
