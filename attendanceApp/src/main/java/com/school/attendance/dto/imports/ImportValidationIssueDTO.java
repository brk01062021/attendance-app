package com.school.attendance.dto.imports;

public class ImportValidationIssueDTO {
    private String sheetName;
    private int rowNumber;
    private String fieldName;
    private String severity;
    private String message;

    public ImportValidationIssueDTO() { }

    public ImportValidationIssueDTO(String sheetName, int rowNumber, String fieldName, String severity, String message) {
        this.sheetName = sheetName;
        this.rowNumber = rowNumber;
        this.fieldName = fieldName;
        this.severity = severity;
        this.message = message;
    }

    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
