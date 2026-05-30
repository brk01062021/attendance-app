package com.school.attendance.dto.onboarding;

import java.util.List;

public class ActivationPackageDTO {
    private String referenceId;
    private String schoolId;
    private String schoolName;
    private String status;
    private String registrationDate;
    private String activatedAt;
    private String credentialsIssuedAt;
    private String message;
    private String nextStep;
    private boolean loginEnabled;
    private List<ActivationCredentialDTO> credentials;
    private List<WorkspaceProvisioningStepDTO> workspaceSteps;
    private List<String> activationChecklist;
    private List<String> importPreparationChecklist;
    private OnboardingStatusResponseDTO statusSummary;

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
    public String getActivatedAt() { return activatedAt; }
    public void setActivatedAt(String activatedAt) { this.activatedAt = activatedAt; }
    public String getCredentialsIssuedAt() { return credentialsIssuedAt; }
    public void setCredentialsIssuedAt(String credentialsIssuedAt) { this.credentialsIssuedAt = credentialsIssuedAt; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
    public boolean isLoginEnabled() { return loginEnabled; }
    public void setLoginEnabled(boolean loginEnabled) { this.loginEnabled = loginEnabled; }
    public List<ActivationCredentialDTO> getCredentials() { return credentials; }
    public void setCredentials(List<ActivationCredentialDTO> credentials) { this.credentials = credentials; }
    public List<WorkspaceProvisioningStepDTO> getWorkspaceSteps() { return workspaceSteps; }
    public void setWorkspaceSteps(List<WorkspaceProvisioningStepDTO> workspaceSteps) { this.workspaceSteps = workspaceSteps; }
    public List<String> getActivationChecklist() { return activationChecklist; }
    public void setActivationChecklist(List<String> activationChecklist) { this.activationChecklist = activationChecklist; }
    public List<String> getImportPreparationChecklist() { return importPreparationChecklist; }
    public void setImportPreparationChecklist(List<String> importPreparationChecklist) { this.importPreparationChecklist = importPreparationChecklist; }
    public OnboardingStatusResponseDTO getStatusSummary() { return statusSummary; }
    public void setStatusSummary(OnboardingStatusResponseDTO statusSummary) { this.statusSummary = statusSummary; }
}
