package com.school.attendance.dto.onboarding;

public class WorkspaceProvisioningStepDTO {
    private String key;
    private String label;
    private String status;
    private String detail;

    public WorkspaceProvisioningStepDTO() {}

    public WorkspaceProvisioningStepDTO(String key, String label, String status, String detail) {
        this.key = key;
        this.label = label;
        this.status = status;
        this.detail = detail;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
