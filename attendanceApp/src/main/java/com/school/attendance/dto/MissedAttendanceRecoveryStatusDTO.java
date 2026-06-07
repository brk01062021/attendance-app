package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class MissedAttendanceRecoveryStatusDTO {
    private String schoolId;
    private String status;
    private String label;
    private String message;
    private String latestRecoveryBatchId;
    private int submittedRows;
    private List<String> auditTrail = new ArrayList<>();

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLatestRecoveryBatchId() { return latestRecoveryBatchId; }
    public void setLatestRecoveryBatchId(String latestRecoveryBatchId) { this.latestRecoveryBatchId = latestRecoveryBatchId; }

    public int getSubmittedRows() { return submittedRows; }
    public void setSubmittedRows(int submittedRows) { this.submittedRows = submittedRows; }

    public List<String> getAuditTrail() { return auditTrail; }
    public void setAuditTrail(List<String> auditTrail) { this.auditTrail = auditTrail == null ? new ArrayList<>() : auditTrail; }
}
