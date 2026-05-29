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
}
