package com.school.attendance.dto.onboarding;

import jakarta.validation.constraints.NotBlank;

public class OnboardingStatusUpdateRequestDTO {
    @NotBlank(message = "Status is required")
    private String status;
    private String reviewNotes;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
}
