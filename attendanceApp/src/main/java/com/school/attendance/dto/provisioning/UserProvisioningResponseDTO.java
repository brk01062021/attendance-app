package com.school.attendance.dto.provisioning;

import java.util.List;

public class UserProvisioningResponseDTO {
    private String schoolId;
    private Long uploadId;
    private String importBatchId;
    private int principalCount;
    private int teacherCount;
    private int studentCount;
    private int parentCount;
    private int createdCount;
    private int updatedCount;
    private String message;
    private List<UserProvisioningCredentialDTO> credentials;

    public UserProvisioningResponseDTO() {}

    public UserProvisioningResponseDTO(String schoolId, Long uploadId, String importBatchId, int principalCount, int teacherCount, int studentCount, int parentCount, int createdCount, int updatedCount, String message, List<UserProvisioningCredentialDTO> credentials) {
        this.schoolId = schoolId;
        this.uploadId = uploadId;
        this.importBatchId = importBatchId;
        this.principalCount = principalCount;
        this.teacherCount = teacherCount;
        this.studentCount = studentCount;
        this.parentCount = parentCount;
        this.createdCount = createdCount;
        this.updatedCount = updatedCount;
        this.message = message;
        this.credentials = credentials;
    }

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public Long getUploadId() { return uploadId; }
    public void setUploadId(Long uploadId) { this.uploadId = uploadId; }
    public String getImportBatchId() { return importBatchId; }
    public void setImportBatchId(String importBatchId) { this.importBatchId = importBatchId; }
    public int getPrincipalCount() { return principalCount; }
    public void setPrincipalCount(int principalCount) { this.principalCount = principalCount; }
    public int getTeacherCount() { return teacherCount; }
    public void setTeacherCount(int teacherCount) { this.teacherCount = teacherCount; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    public int getParentCount() { return parentCount; }
    public void setParentCount(int parentCount) { this.parentCount = parentCount; }
    public int getCreatedCount() { return createdCount; }
    public void setCreatedCount(int createdCount) { this.createdCount = createdCount; }
    public int getUpdatedCount() { return updatedCount; }
    public void setUpdatedCount(int updatedCount) { this.updatedCount = updatedCount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<UserProvisioningCredentialDTO> getCredentials() { return credentials; }
    public void setCredentials(List<UserProvisioningCredentialDTO> credentials) { this.credentials = credentials; }
}
