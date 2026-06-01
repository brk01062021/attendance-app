package com.school.attendance.dto;

import java.time.LocalDateTime;

public class WorkspaceActivationAuditDTO {
    private String eventType;
    private String title;
    private String description;
    private String status;
    private LocalDateTime eventAt;

    public WorkspaceActivationAuditDTO() {}

    public WorkspaceActivationAuditDTO(String eventType, String title, String description, String status, LocalDateTime eventAt) {
        this.eventType = eventType;
        this.title = title;
        this.description = description;
        this.status = status;
        this.eventAt = eventAt;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getEventAt() { return eventAt; }
    public void setEventAt(LocalDateTime eventAt) { this.eventAt = eventAt; }
}
