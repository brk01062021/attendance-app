package com.school.attendance.dto;

public class ActivityApprovalRequest {
    private Long actionBy;
    private String remarks;

    public Long getActionBy() { return actionBy; }
    public void setActionBy(Long actionBy) { this.actionBy = actionBy; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
