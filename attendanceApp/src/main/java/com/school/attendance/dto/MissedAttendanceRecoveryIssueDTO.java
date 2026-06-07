package com.school.attendance.dto;

public class MissedAttendanceRecoveryIssueDTO {
    private int rowNumber;
    private String category;
    private String severity;
    private String message;

    public MissedAttendanceRecoveryIssueDTO() {}
    public MissedAttendanceRecoveryIssueDTO(int rowNumber, String category, String severity, String message) {
        this.rowNumber = rowNumber; this.category = category; this.severity = severity; this.message = message;
    }
    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
