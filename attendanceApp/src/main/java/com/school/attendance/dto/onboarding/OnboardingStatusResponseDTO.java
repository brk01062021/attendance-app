package com.school.attendance.dto.onboarding;

public class OnboardingStatusResponseDTO {
    private String referenceId;
    private String schoolId;
    private String schoolName;
    private String requestType;
    private String status;
    private String message;
    private String nextStep;
    private boolean loginEnabled;
    private boolean importEnabled;
    private String registrationDate;
    private String submittedAt;
    private String approvedAt;
    private String pilotActivatedAt;
    private String activatedAt;
    private String submittedBy;
    private String approvedBy;
    private String pilotEnabledBy;
    private String activatedBy;
    private String credentialsIssuedBy;
    private String credentialsIssuedAt;
    private String statusHistory;

    public OnboardingStatusResponseDTO() {}

    public OnboardingStatusResponseDTO(String referenceId, String schoolId, String schoolName, String requestType, String status, String message, String nextStep, boolean loginEnabled, boolean importEnabled) {
        this.referenceId = referenceId;
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.requestType = requestType;
        this.status = status;
        this.message = message;
        this.nextStep = nextStep;
        this.loginEnabled = loginEnabled;
        this.importEnabled = importEnabled;
    }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
    public boolean isLoginEnabled() { return loginEnabled; }
    public void setLoginEnabled(boolean loginEnabled) { this.loginEnabled = loginEnabled; }
    public boolean isImportEnabled() { return importEnabled; }
    public void setImportEnabled(boolean importEnabled) { this.importEnabled = importEnabled; }
    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }
    public String getPilotActivatedAt() { return pilotActivatedAt; }
    public void setPilotActivatedAt(String pilotActivatedAt) { this.pilotActivatedAt = pilotActivatedAt; }
    public String getActivatedAt() { return activatedAt; }
    public void setActivatedAt(String activatedAt) { this.activatedAt = activatedAt; }
    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getPilotEnabledBy() { return pilotEnabledBy; }
    public void setPilotEnabledBy(String pilotEnabledBy) { this.pilotEnabledBy = pilotEnabledBy; }
    public String getActivatedBy() { return activatedBy; }
    public void setActivatedBy(String activatedBy) { this.activatedBy = activatedBy; }
    public String getCredentialsIssuedBy() { return credentialsIssuedBy; }
    public void setCredentialsIssuedBy(String credentialsIssuedBy) { this.credentialsIssuedBy = credentialsIssuedBy; }
    public String getCredentialsIssuedAt() { return credentialsIssuedAt; }
    public void setCredentialsIssuedAt(String credentialsIssuedAt) { this.credentialsIssuedAt = credentialsIssuedAt; }
    public String getStatusHistory() { return statusHistory; }
    public void setStatusHistory(String statusHistory) { this.statusHistory = statusHistory; }
}
