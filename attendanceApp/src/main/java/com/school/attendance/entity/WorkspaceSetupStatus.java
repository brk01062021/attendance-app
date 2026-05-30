package com.school.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_setup_status", indexes = {
        @Index(name = "idx_workspace_setup_school_id", columnList = "schoolId", unique = true)
})
public class WorkspaceSetupStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 4)
    private String schoolId;

    private String schoolName;
    private String academicYear;
    private LocalDate academicYearStartDate;
    private LocalDate academicYearEndDate;
    private String workingDays;
    private String schoolStartTime;
    private String schoolEndTime;
    private Integer periodsPerDay;

    private boolean schoolProfileCompleted;
    private boolean academicYearCompleted;
    private boolean workingDaysCompleted;
    private boolean schoolTimingsCompleted;
    private boolean classesCompleted;
    private boolean sectionsCompleted;
    private boolean teachersCompleted;
    private boolean subjectsCompleted;
    private boolean holidayCalendarCompleted;

    private int completedSteps;
    private int totalSteps = 9;
    private int progressPercent;
    private boolean importLocked = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        recalculate();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
        recalculate();
    }

    public void recalculate() {
        int count = 0;
        if (schoolProfileCompleted) count++;
        if (academicYearCompleted) count++;
        if (workingDaysCompleted) count++;
        if (schoolTimingsCompleted) count++;
        if (classesCompleted) count++;
        if (sectionsCompleted) count++;
        if (teachersCompleted) count++;
        if (subjectsCompleted) count++;
        if (holidayCalendarCompleted) count++;
        completedSteps = count;
        totalSteps = 9;
        progressPercent = Math.round((count * 100f) / totalSteps);
        importLocked = count < totalSteps;
    }

    public Long getId() { return id; }
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
    public boolean isSchoolProfileCompleted() { return schoolProfileCompleted; }
    public void setSchoolProfileCompleted(boolean schoolProfileCompleted) { this.schoolProfileCompleted = schoolProfileCompleted; }
    public boolean isAcademicYearCompleted() { return academicYearCompleted; }
    public void setAcademicYearCompleted(boolean academicYearCompleted) { this.academicYearCompleted = academicYearCompleted; }
    public boolean isWorkingDaysCompleted() { return workingDaysCompleted; }
    public void setWorkingDaysCompleted(boolean workingDaysCompleted) { this.workingDaysCompleted = workingDaysCompleted; }
    public boolean isSchoolTimingsCompleted() { return schoolTimingsCompleted; }
    public void setSchoolTimingsCompleted(boolean schoolTimingsCompleted) { this.schoolTimingsCompleted = schoolTimingsCompleted; }
    public boolean isClassesCompleted() { return classesCompleted; }
    public void setClassesCompleted(boolean classesCompleted) { this.classesCompleted = classesCompleted; }
    public boolean isSectionsCompleted() { return sectionsCompleted; }
    public void setSectionsCompleted(boolean sectionsCompleted) { this.sectionsCompleted = sectionsCompleted; }
    public boolean isTeachersCompleted() { return teachersCompleted; }
    public void setTeachersCompleted(boolean teachersCompleted) { this.teachersCompleted = teachersCompleted; }
    public boolean isSubjectsCompleted() { return subjectsCompleted; }
    public void setSubjectsCompleted(boolean subjectsCompleted) { this.subjectsCompleted = subjectsCompleted; }
    public boolean isHolidayCalendarCompleted() { return holidayCalendarCompleted; }
    public void setHolidayCalendarCompleted(boolean holidayCalendarCompleted) { this.holidayCalendarCompleted = holidayCalendarCompleted; }
    public int getCompletedSteps() { return completedSteps; }
    public int getTotalSteps() { return totalSteps; }
    public int getProgressPercent() { return progressPercent; }
    public boolean isImportLocked() { return importLocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
