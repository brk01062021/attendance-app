package com.school.attendance.dto;

public class ExistingTimetableImportStatusDTO {
    private String schoolId;
    private String status;
    private String label;
    private String message;
    private String importBatchId;
    private String publishedBatchId;
    private Integer totalClasses;
    private Integer totalSections;
    private Integer totalTeachers;
    private Integer totalPeriodAllocations;

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getImportBatchId() { return importBatchId; }
    public void setImportBatchId(String importBatchId) { this.importBatchId = importBatchId; }
    public String getPublishedBatchId() { return publishedBatchId; }
    public void setPublishedBatchId(String publishedBatchId) { this.publishedBatchId = publishedBatchId; }
    public Integer getTotalClasses() { return totalClasses; }
    public void setTotalClasses(Integer totalClasses) { this.totalClasses = totalClasses; }
    public Integer getTotalSections() { return totalSections; }
    public void setTotalSections(Integer totalSections) { this.totalSections = totalSections; }
    public Integer getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(Integer totalTeachers) { this.totalTeachers = totalTeachers; }
    public Integer getTotalPeriodAllocations() { return totalPeriodAllocations; }
    public void setTotalPeriodAllocations(Integer totalPeriodAllocations) { this.totalPeriodAllocations = totalPeriodAllocations; }
}
