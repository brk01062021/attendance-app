package com.school.attendance.dto.onboarding;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SchoolRegistrationRequestDTO {
    @NotBlank(message = "School name is required")
    private String schoolName;

    @NotBlank(message = "Requested school ID is required")
    @Pattern(regexp = "^[A-Z0-9]{4}$", message = "School ID must be exactly 4 uppercase letters/numbers")
    private String requestedSchoolId;

    @NotBlank(message = "Contact person is required")
    private String contactPerson;

    @NotBlank(message = "Contact phone is required")
    @Size(min = 7, max = 20, message = "Contact phone must be valid")
    private String contactPhone;

    @Email(message = "Email must be valid")
    private String contactEmail;

    private String city;
    private String state;
    private Integer expectedStudents;
    private Integer expectedTeachers;
    private String notes;

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getRequestedSchoolId() { return requestedSchoolId; }
    public void setRequestedSchoolId(String requestedSchoolId) { this.requestedSchoolId = requestedSchoolId; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
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
}
