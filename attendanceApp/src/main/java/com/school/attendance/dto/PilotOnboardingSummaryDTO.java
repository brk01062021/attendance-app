package com.school.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public class PilotOnboardingSummaryDTO {
    private Long schoolId;
    private String schoolName;
    private int targetStudents;
    private int targetTeachers;
    private int targetAdmins;
    private int targetPrincipals;
    private String readinessStatus;
    private LocalDate plannedPilotStartDate;
    private List<PilotOnboardingStepDTO> steps;

    public PilotOnboardingSummaryDTO(Long schoolId,
                                     String schoolName,
                                     int targetStudents,
                                     int targetTeachers,
                                     int targetAdmins,
                                     int targetPrincipals,
                                     String readinessStatus,
                                     LocalDate plannedPilotStartDate,
                                     List<PilotOnboardingStepDTO> steps) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.targetStudents = targetStudents;
        this.targetTeachers = targetTeachers;
        this.targetAdmins = targetAdmins;
        this.targetPrincipals = targetPrincipals;
        this.readinessStatus = readinessStatus;
        this.plannedPilotStartDate = plannedPilotStartDate;
        this.steps = steps;
    }

    public Long getSchoolId() { return schoolId; }
    public String getSchoolName() { return schoolName; }
    public int getTargetStudents() { return targetStudents; }
    public int getTargetTeachers() { return targetTeachers; }
    public int getTargetAdmins() { return targetAdmins; }
    public int getTargetPrincipals() { return targetPrincipals; }
    public String getReadinessStatus() { return readinessStatus; }
    public LocalDate getPlannedPilotStartDate() { return plannedPilotStartDate; }
    public List<PilotOnboardingStepDTO> getSteps() { return steps; }
}
