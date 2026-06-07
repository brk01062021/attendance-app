package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class MissedAttendanceRecoveryResponseDTO {
    private String recoveryBatchId;
    private String schoolId;
    private String status;
    private String message;
    private boolean valid;
    private boolean canSubmit;
    private int totalRows;
    private int acceptedRows;
    private int submittedRows;
    private int errorCount;
    private int warningCount;
    private List<MissedAttendanceRecoveryRowDTO> rows = new ArrayList<>();
    private List<MissedAttendanceRecoveryIssueDTO> issues = new ArrayList<>();
    private List<MissedAttendanceRecoveryCardDTO> validationCards = new ArrayList<>();

    public String getRecoveryBatchId() { return recoveryBatchId; }
    public void setRecoveryBatchId(String recoveryBatchId) { this.recoveryBatchId = recoveryBatchId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public boolean isCanSubmit() { return canSubmit; }
    public void setCanSubmit(boolean canSubmit) { this.canSubmit = canSubmit; }
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getAcceptedRows() { return acceptedRows; }
    public void setAcceptedRows(int acceptedRows) { this.acceptedRows = acceptedRows; }
    public int getSubmittedRows() { return submittedRows; }
    public void setSubmittedRows(int submittedRows) { this.submittedRows = submittedRows; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    public int getWarningCount() { return warningCount; }
    public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
    public List<MissedAttendanceRecoveryRowDTO> getRows() { return rows; }
    public void setRows(List<MissedAttendanceRecoveryRowDTO> rows) { this.rows = rows == null ? new ArrayList<>() : rows; }
    public List<MissedAttendanceRecoveryIssueDTO> getIssues() { return issues; }
    public void setIssues(List<MissedAttendanceRecoveryIssueDTO> issues) { this.issues = issues == null ? new ArrayList<>() : issues; }
    public List<MissedAttendanceRecoveryCardDTO> getValidationCards() { return validationCards; }
    public void setValidationCards(List<MissedAttendanceRecoveryCardDTO> validationCards) { this.validationCards = validationCards == null ? new ArrayList<>() : validationCards; }
}
