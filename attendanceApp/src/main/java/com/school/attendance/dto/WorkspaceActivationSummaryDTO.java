package com.school.attendance.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceActivationSummaryDTO {
    private String schoolId;
    private String schoolName;
    private String academicYear;
    private String activationStatus;
    private String activationMessage;
    private boolean schoolProfileReady;
    private boolean academicYearReady;
    private boolean workspaceSetupReady;
    private boolean importCommitted;
    private boolean readyForActivation;
    private int readinessPercent;
    private int committedWorkbookCount;
    private LocalDateTime lastWorkbookCommittedAt;
    private WorkspaceChecklistDTO workspaceChecklist;
    private List<WorkspaceHealthItemDTO> healthItems = new ArrayList<>();
    private List<WorkspaceActivationAuditDTO> auditTrail = new ArrayList<>();

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getActivationStatus() { return activationStatus; }
    public void setActivationStatus(String activationStatus) { this.activationStatus = activationStatus; }
    public String getActivationMessage() { return activationMessage; }
    public void setActivationMessage(String activationMessage) { this.activationMessage = activationMessage; }
    public boolean isSchoolProfileReady() { return schoolProfileReady; }
    public void setSchoolProfileReady(boolean schoolProfileReady) { this.schoolProfileReady = schoolProfileReady; }
    public boolean isAcademicYearReady() { return academicYearReady; }
    public void setAcademicYearReady(boolean academicYearReady) { this.academicYearReady = academicYearReady; }
    public boolean isWorkspaceSetupReady() { return workspaceSetupReady; }
    public void setWorkspaceSetupReady(boolean workspaceSetupReady) { this.workspaceSetupReady = workspaceSetupReady; }
    public boolean isImportCommitted() { return importCommitted; }
    public void setImportCommitted(boolean importCommitted) { this.importCommitted = importCommitted; }
    public boolean isReadyForActivation() { return readyForActivation; }
    public void setReadyForActivation(boolean readyForActivation) { this.readyForActivation = readyForActivation; }
    public int getReadinessPercent() { return readinessPercent; }
    public void setReadinessPercent(int readinessPercent) { this.readinessPercent = readinessPercent; }
    public int getCommittedWorkbookCount() { return committedWorkbookCount; }
    public void setCommittedWorkbookCount(int committedWorkbookCount) { this.committedWorkbookCount = committedWorkbookCount; }
    public LocalDateTime getLastWorkbookCommittedAt() { return lastWorkbookCommittedAt; }
    public void setLastWorkbookCommittedAt(LocalDateTime lastWorkbookCommittedAt) { this.lastWorkbookCommittedAt = lastWorkbookCommittedAt; }
    public WorkspaceChecklistDTO getWorkspaceChecklist() { return workspaceChecklist; }
    public void setWorkspaceChecklist(WorkspaceChecklistDTO workspaceChecklist) { this.workspaceChecklist = workspaceChecklist; }
    public List<WorkspaceHealthItemDTO> getHealthItems() { return healthItems; }
    public void setHealthItems(List<WorkspaceHealthItemDTO> healthItems) { this.healthItems = healthItems; }
    public List<WorkspaceActivationAuditDTO> getAuditTrail() { return auditTrail; }
    public void setAuditTrail(List<WorkspaceActivationAuditDTO> auditTrail) { this.auditTrail = auditTrail; }
}
