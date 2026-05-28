package com.school.attendance.dto.onboarding;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PilotDemoRequestDTO {
    @NotBlank(message = "School name is required")
    private String schoolName;

    @NotBlank(message = "Contact person is required")
    private String contactPerson;

    @NotBlank(message = "Contact phone is required")
    @Size(min = 7, max = 20, message = "Contact phone must be valid")
    private String contactPhone;

    @Email(message = "Email must be valid")
    private String contactEmail;

    private String preferredRole;
    private String city;
    private String state;
    private Integer expectedStudents;
    private String notes;

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
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
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
