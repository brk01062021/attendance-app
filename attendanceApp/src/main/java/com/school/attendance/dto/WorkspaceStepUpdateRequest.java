package com.school.attendance.dto;

import java.time.LocalDate;

public class WorkspaceStepUpdateRequest {
    private String schoolName;
    private String academicYear;
    private LocalDate academicYearStartDate;
    private LocalDate academicYearEndDate;
    private String workingDays;
    private String schoolStartTime;
    private String schoolEndTime;
    private Integer periodsPerDay;
    private Boolean completed;

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
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
