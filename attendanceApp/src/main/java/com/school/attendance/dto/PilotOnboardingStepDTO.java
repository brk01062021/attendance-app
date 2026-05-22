package com.school.attendance.dto;

public class PilotOnboardingStepDTO {
    private String key;
    private String title;
    private String owner;
    private String status;
    private String priority;
    private String detail;

    public PilotOnboardingStepDTO(String key, String title, String owner, String status, String priority, String detail) {
        this.key = key;
        this.title = title;
        this.owner = owner;
        this.status = status;
        this.priority = priority;
        this.detail = detail;
    }

    public String getKey() { return key; }
    public String getTitle() { return title; }
    public String getOwner() { return owner; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getDetail() { return detail; }
}
