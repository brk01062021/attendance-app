package com.school.attendance.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivationOperationsCenterDTO {
    private String schoolId;
    private String schoolName;
    private String activationStatus;
    private String reportingStatus;
    private int readinessPercent;
    private boolean readyForActivation;
    private boolean tenantActive;
    private String operationsNote;
    private LocalDateTime generatedAt = LocalDateTime.now();
    private List<ActivationOperationStepDTO> timeline = new ArrayList<>();
    private List<String> notesHistory = new ArrayList<>();
    private List<WorkspaceHealthItemDTO> adminPrincipalReportCards = new ArrayList<>();

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getActivationStatus() { return activationStatus; }
    public void setActivationStatus(String activationStatus) { this.activationStatus = activationStatus; }
    public String getReportingStatus() { return reportingStatus; }
    public void setReportingStatus(String reportingStatus) { this.reportingStatus = reportingStatus; }
    public int getReadinessPercent() { return readinessPercent; }
    public void setReadinessPercent(int readinessPercent) { this.readinessPercent = readinessPercent; }
    public boolean isReadyForActivation() { return readyForActivation; }
    public void setReadyForActivation(boolean readyForActivation) { this.readyForActivation = readyForActivation; }
    public boolean isTenantActive() { return tenantActive; }
    public void setTenantActive(boolean tenantActive) { this.tenantActive = tenantActive; }
    public String getOperationsNote() { return operationsNote; }
    public void setOperationsNote(String operationsNote) { this.operationsNote = operationsNote; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public List<ActivationOperationStepDTO> getTimeline() { return timeline; }
    public void setTimeline(List<ActivationOperationStepDTO> timeline) { this.timeline = timeline; }
    public List<String> getNotesHistory() { return notesHistory; }
    public void setNotesHistory(List<String> notesHistory) { this.notesHistory = notesHistory; }
    public List<WorkspaceHealthItemDTO> getAdminPrincipalReportCards() { return adminPrincipalReportCards; }
    public void setAdminPrincipalReportCards(List<WorkspaceHealthItemDTO> adminPrincipalReportCards) { this.adminPrincipalReportCards = adminPrincipalReportCards; }
}
