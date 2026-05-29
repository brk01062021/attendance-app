package com.school.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_onboarding_requests", indexes = {
        @Index(name = "idx_school_onboarding_school_id", columnList = "schoolId"),
        @Index(name = "idx_school_onboarding_reference_id", columnList = "referenceId", unique = true),
        @Index(name = "idx_school_onboarding_status", columnList = "status")
})
public class SchoolOnboardingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String referenceId;

    @Column(length = 4)
    private String schoolId;

    @Column(nullable = false)
    private String schoolName;

    @Column(nullable = false, length = 24)
    private String requestType;

    @Column(nullable = false, length = 24)
    private String status;

    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String preferredRole;
    private String city;
    private String state;
    private Integer expectedStudents;
    private Integer expectedTeachers;

    @Column(length = 2000)
    private String notes;

    @Column(length = 2000)
    private String reviewNotes;

    @Column(length = 4000)
    private String statusHistory;

    private String submittedBy;
    private String approvedBy;
    private String pilotEnabledBy;
    private String activatedBy;
    private String credentialsIssuedBy;
    private String adminUsername;
    private String adminInitialPassword;
    private String principalUsername;
    private String principalInitialPassword;
    private LocalDateTime credentialsIssuedAt;

    private LocalDateTime reservedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime pilotActivatedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
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
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getPreferredRole() { return preferredRole; }
    public void setPreferredRole(String preferredRole) { this.preferredRole = preferredRole; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Integer getExpectedStudents() { return expectedStudents; }
    public void setExpectedStudents(Integer expectedStudents) { this.expectedStudents = expectedStudents; }
    public Integer getExpectedTeachers() { return expectedTeachers; }
    public void setExpectedTeachers(Integer expectedTeachers) { this.expectedTeachers = expectedTeachers; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public String getStatusHistory() { return statusHistory; }
    public void setStatusHistory(String statusHistory) { this.statusHistory = statusHistory; }
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
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getAdminInitialPassword() { return adminInitialPassword; }
    public void setAdminInitialPassword(String adminInitialPassword) { this.adminInitialPassword = adminInitialPassword; }
    public String getPrincipalUsername() { return principalUsername; }
    public void setPrincipalUsername(String principalUsername) { this.principalUsername = principalUsername; }
    public String getPrincipalInitialPassword() { return principalInitialPassword; }
    public void setPrincipalInitialPassword(String principalInitialPassword) { this.principalInitialPassword = principalInitialPassword; }
    public LocalDateTime getCredentialsIssuedAt() { return credentialsIssuedAt; }
    public void setCredentialsIssuedAt(LocalDateTime credentialsIssuedAt) { this.credentialsIssuedAt = credentialsIssuedAt; }

    public LocalDateTime getReservedAt() { return reservedAt; }
    public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getPilotActivatedAt() { return pilotActivatedAt; }
    public void setPilotActivatedAt(LocalDateTime pilotActivatedAt) { this.pilotActivatedAt = pilotActivatedAt; }
    public LocalDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
