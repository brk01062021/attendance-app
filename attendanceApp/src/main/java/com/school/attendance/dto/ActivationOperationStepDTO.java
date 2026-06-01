package com.school.attendance.dto;

import java.time.LocalDateTime;

public class ActivationOperationStepDTO {
    private String stepKey;
    private String title;
    private String status;
    private String note;
    private LocalDateTime eventAt;

    public ActivationOperationStepDTO() { }
    public ActivationOperationStepDTO(String stepKey, String title, String status, String note, LocalDateTime eventAt) {
        this.stepKey = stepKey;
        this.title = title;
        this.status = status;
        this.note = note;
        this.eventAt = eventAt;
    }
    public String getStepKey() { return stepKey; }
    public void setStepKey(String stepKey) { this.stepKey = stepKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getEventAt() { return eventAt; }
    public void setEventAt(LocalDateTime eventAt) { this.eventAt = eventAt; }
}
