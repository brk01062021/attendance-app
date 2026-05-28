package com.school.attendance.dto.onboarding;

public class SchoolRegistrationResponseDTO {
    private String referenceId;
    private String schoolId;
    private String schoolName;
    private String status;
    private String message;
    private String nextStep;

    public SchoolRegistrationResponseDTO() {}

    public SchoolRegistrationResponseDTO(String referenceId, String schoolId, String schoolName, String status, String message, String nextStep) {
        this.referenceId = referenceId;
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.status = status;
        this.message = message;
        this.nextStep = nextStep;
    }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
}
