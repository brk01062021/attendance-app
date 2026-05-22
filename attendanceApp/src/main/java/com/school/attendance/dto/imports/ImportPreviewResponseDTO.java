package com.school.attendance.dto.imports;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImportPreviewResponseDTO {
    private String schoolId;
    private String importType;
    private boolean valid;
    private Map<String, Integer> rowCounts = new LinkedHashMap<>();
    private List<ImportValidationIssueDTO> issues = new ArrayList<>();
    private Instant previewedAt = Instant.now();

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public Map<String, Integer> getRowCounts() { return rowCounts; }
    public void setRowCounts(Map<String, Integer> rowCounts) { this.rowCounts = rowCounts; }
    public List<ImportValidationIssueDTO> getIssues() { return issues; }
    public void setIssues(List<ImportValidationIssueDTO> issues) { this.issues = issues; }
    public Instant getPreviewedAt() { return previewedAt; }
    public void setPreviewedAt(Instant previewedAt) { this.previewedAt = previewedAt; }
}
