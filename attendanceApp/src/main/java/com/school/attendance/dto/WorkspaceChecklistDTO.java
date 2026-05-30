package com.school.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class WorkspaceChecklistDTO {
    private String schoolId;
    private String schoolName;
    private String academicYear;
    private LocalDate academicYearStartDate;
    private LocalDate academicYearEndDate;
    private String workingDays;
    private String schoolStartTime;
    private String schoolEndTime;
    private Integer periodsPerDay;
    private int completedSteps;
    private int totalSteps;
    private int progressPercent;
    private boolean importLocked;
    private String importLockMessage;
    private List<WorkspaceStepDTO> steps;
    private LocalDateTime updatedAt;

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public LocalDate getAcademicYearStartDate() { return academicYearStartDate; }
    public void setAcademicYearStartDate(LocalDate academicYearStartDate) { this.academicYearStartDate = academicYearStartDate; }
    public LocalDate getAcademicYearEndDate() { return academicYearEndDate; }
    public void setAcademicYearEndDate(LocalDate academicYearEndDate) { this.academicYearEndDate = academicYearEndDate; }
    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }
    public String getSchoolStartTime() { return schoolStartTime; }
    public void setSchoolStartTime(String schoolStartTime) { this.schoolStartTime = schoolStartTime; }
    public String getSchoolEndTime() { return schoolEndTime; }
    public void setSchoolEndTime(String schoolEndTime) { this.schoolEndTime = schoolEndTime; }
    public Integer getPeriodsPerDay() { return periodsPerDay; }
    public void setPeriodsPerDay(Integer periodsPerDay) { this.periodsPerDay = periodsPerDay; }
    public int getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(int completedSteps) { this.completedSteps = completedSteps; }
    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public boolean isImportLocked() { return importLocked; }
    public void setImportLocked(boolean importLocked) { this.importLocked = importLocked; }
    public String getImportLockMessage() { return importLockMessage; }
    public void setImportLockMessage(String importLockMessage) { this.importLockMessage = importLockMessage; }
    public List<WorkspaceStepDTO> getSteps() { return steps; }
    public void setSteps(List<WorkspaceStepDTO> steps) { this.steps = steps; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
