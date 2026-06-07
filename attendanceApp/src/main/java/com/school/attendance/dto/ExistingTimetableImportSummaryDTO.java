package com.school.attendance.dto;

public class ExistingTimetableImportSummaryDTO {
    private String label;
    private Integer count;
    private String severity;
    private String guidance;

    public ExistingTimetableImportSummaryDTO() {}

    public ExistingTimetableImportSummaryDTO(String label, Integer count, String severity, String guidance) {
        this.label = label;
        this.count = count;
        this.severity = severity;
        this.guidance = guidance;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getGuidance() { return guidance; }
    public void setGuidance(String guidance) { this.guidance = guidance; }
}
