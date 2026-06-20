package com.school.attendance.dto;

public class ParentOtpResponse {
    private boolean success;
    private String message;
    private String schoolId;
    private String studentId;
    private String parentMobile;
    private String maskedMobile;
    private String devOtp;

    public ParentOtpResponse() {}

    public ParentOtpResponse(boolean success, String message, String schoolId, String studentId, String parentMobile, String maskedMobile, String devOtp) {
        this.success = success;
        this.message = message;
        this.schoolId = schoolId;
        this.studentId = studentId;
        this.parentMobile = parentMobile;
        this.maskedMobile = maskedMobile;
        this.devOtp = devOtp;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getParentMobile() { return parentMobile; }
    public void setParentMobile(String parentMobile) { this.parentMobile = parentMobile; }
    public String getMaskedMobile() { return maskedMobile; }
    public void setMaskedMobile(String maskedMobile) { this.maskedMobile = maskedMobile; }
    public String getDevOtp() { return devOtp; }
    public void setDevOtp(String devOtp) { this.devOtp = devOtp; }
}
