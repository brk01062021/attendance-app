package com.school.attendance.dto;

public class MissedAttendanceRecoveryCardDTO {
    private String label;
    private int count;
    private String severity;
    private String guidance;
    public MissedAttendanceRecoveryCardDTO() {}
    public MissedAttendanceRecoveryCardDTO(String label, int count, String severity, String guidance) { this.label=label; this.count=count; this.severity=severity; this.guidance=guidance; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getGuidance() { return guidance; }
    public void setGuidance(String guidance) { this.guidance = guidance; }
}
