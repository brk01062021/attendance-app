package com.school.attendance.dto.onboarding;

public class SchoolIdAvailabilityResponseDTO {
    private String schoolId;
    private boolean available;
    private String status;
    private String message;

    public SchoolIdAvailabilityResponseDTO() {}

    public SchoolIdAvailabilityResponseDTO(String schoolId, boolean available, String status, String message) {
        this.schoolId = schoolId;
        this.available = available;
        this.status = status;
        this.message = message;
    }

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
