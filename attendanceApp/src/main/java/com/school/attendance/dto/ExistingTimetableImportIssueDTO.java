package com.school.attendance.dto;

public class ExistingTimetableImportIssueDTO {
    private Integer rowNumber;
    private String severity;
    private String fieldName;
    private String category;
    private String message;

    public ExistingTimetableImportIssueDTO() {}

    public ExistingTimetableImportIssueDTO(Integer rowNumber, String severity, String fieldName, String message) {
        this(rowNumber, severity, fieldName, fieldName, message);
    }

    public ExistingTimetableImportIssueDTO(Integer rowNumber, String severity, String fieldName, String category, String message) {
        this.rowNumber = rowNumber;
        this.severity = severity;
        this.fieldName = fieldName;
        this.category = category;
        this.message = message;
    }

    public Integer getRowNumber() { return rowNumber; }
    public void setRowNumber(Integer rowNumber) { this.rowNumber = rowNumber; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
