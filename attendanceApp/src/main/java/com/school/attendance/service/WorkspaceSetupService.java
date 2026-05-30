package com.school.attendance.service;

import com.school.attendance.dto.WorkspaceChecklistDTO;
import com.school.attendance.dto.WorkspaceStepDTO;
import com.school.attendance.dto.WorkspaceStepUpdateRequest;
import com.school.attendance.entity.WorkspaceSetupStatus;
import com.school.attendance.repository.WorkspaceSetupStatusRepository;
import com.school.attendance.tenant.TenantUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class WorkspaceSetupService {
    private final WorkspaceSetupStatusRepository repository;

    public WorkspaceSetupService(WorkspaceSetupStatusRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WorkspaceChecklistDTO getOrCreate(String schoolId) {
        WorkspaceSetupStatus status = repository.findBySchoolIdIgnoreCase(TenantUtils.requireValidSchoolId(schoolId))
                .orElseGet(() -> {
                    WorkspaceSetupStatus created = new WorkspaceSetupStatus();
                    created.setSchoolId(TenantUtils.requireValidSchoolId(schoolId));
                    return repository.save(created);
                });
        status.recalculate();
        return toDto(status);
    }

    @Transactional
    public WorkspaceChecklistDTO updateStep(String schoolId, String stepKey, WorkspaceStepUpdateRequest request) {
        WorkspaceSetupStatus status = repository.findBySchoolIdIgnoreCase(TenantUtils.requireValidSchoolId(schoolId))
                .orElseGet(() -> {
                    WorkspaceSetupStatus created = new WorkspaceSetupStatus();
                    created.setSchoolId(TenantUtils.requireValidSchoolId(schoolId));
                    return created;
                });
        String key = stepKey == null ? "" : stepKey.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        boolean completed = request == null || request.getCompleted() == null || request.getCompleted();

        switch (key) {
            case "SCHOOL_PROFILE" -> {
                status.setSchoolName(clean(request.getSchoolName(), status.getSchoolName()));
                status.setSchoolProfileCompleted(completed && notBlank(status.getSchoolName()));
            }
            case "ACADEMIC_YEAR" -> {
                status.setAcademicYear(clean(request.getAcademicYear(), status.getAcademicYear()));
                status.setAcademicYearStartDate(request.getAcademicYearStartDate() != null ? request.getAcademicYearStartDate() : status.getAcademicYearStartDate());
                status.setAcademicYearEndDate(request.getAcademicYearEndDate() != null ? request.getAcademicYearEndDate() : status.getAcademicYearEndDate());
                status.setAcademicYearCompleted(completed && notBlank(status.getAcademicYear()));
            }
            case "WORKING_DAYS" -> {
                status.setWorkingDays(clean(request.getWorkingDays(), status.getWorkingDays()));
                status.setWorkingDaysCompleted(completed && notBlank(status.getWorkingDays()));
            }
            case "SCHOOL_TIMINGS" -> {
                status.setSchoolStartTime(clean(request.getSchoolStartTime(), status.getSchoolStartTime()));
                status.setSchoolEndTime(clean(request.getSchoolEndTime(), status.getSchoolEndTime()));
                status.setPeriodsPerDay(request.getPeriodsPerDay() != null ? request.getPeriodsPerDay() : status.getPeriodsPerDay());
                status.setSchoolTimingsCompleted(completed && notBlank(status.getSchoolStartTime()) && notBlank(status.getSchoolEndTime()));
            }
            case "CLASSES" -> status.setClassesCompleted(completed);
            case "SECTIONS" -> status.setSectionsCompleted(completed);
            case "TEACHERS" -> status.setTeachersCompleted(completed);
            case "SUBJECTS" -> status.setSubjectsCompleted(completed);
            case "HOLIDAY_CALENDAR" -> status.setHolidayCalendarCompleted(completed);
            default -> throw new IllegalArgumentException("Unsupported workspace setup step: " + stepKey);
        }
        status.recalculate();
        return toDto(repository.save(status));
    }

    public boolean isImportLocked(String schoolId) {
        WorkspaceChecklistDTO checklist = getOrCreate(schoolId);
        return checklist.isImportLocked();
    }

    public void requireImportUnlocked(String schoolId) {
        WorkspaceChecklistDTO checklist = getOrCreate(schoolId);
        if (checklist.isImportLocked()) {
            throw new IllegalStateException(checklist.getImportLockMessage());
        }
    }

    private WorkspaceChecklistDTO toDto(WorkspaceSetupStatus s) {
        WorkspaceChecklistDTO dto = new WorkspaceChecklistDTO();
        dto.setSchoolId(s.getSchoolId());
        dto.setSchoolName(s.getSchoolName());
        dto.setAcademicYear(s.getAcademicYear());
        dto.setAcademicYearStartDate(s.getAcademicYearStartDate());
        dto.setAcademicYearEndDate(s.getAcademicYearEndDate());
        dto.setWorkingDays(s.getWorkingDays());
        dto.setSchoolStartTime(s.getSchoolStartTime());
        dto.setSchoolEndTime(s.getSchoolEndTime());
        dto.setPeriodsPerDay(s.getPeriodsPerDay());
        dto.setCompletedSteps(s.getCompletedSteps());
        dto.setTotalSteps(s.getTotalSteps());
        dto.setProgressPercent(s.getProgressPercent());
        dto.setImportLocked(s.isImportLocked());
        dto.setImportLockMessage(s.isImportLocked()
                ? "Complete Workspace Initialization before importing school data. Required order: School Profile, Academic Year, Working Days, School Timings, Classes, Sections, Teachers, Subjects, Holiday Calendar."
                : "Workspace Initialization complete. Import School Data is unlocked.");
        dto.setUpdatedAt(s.getUpdatedAt());
        dto.setSteps(List.of(
                new WorkspaceStepDTO("SCHOOL_PROFILE", "School Profile", s.isSchoolProfileCompleted(), true),
                new WorkspaceStepDTO("ACADEMIC_YEAR", "Academic Year", s.isAcademicYearCompleted(), true),
                new WorkspaceStepDTO("WORKING_DAYS", "Working Days", s.isWorkingDaysCompleted(), true),
                new WorkspaceStepDTO("SCHOOL_TIMINGS", "School Timings", s.isSchoolTimingsCompleted(), true),
                new WorkspaceStepDTO("CLASSES", "Classes", s.isClassesCompleted(), true),
                new WorkspaceStepDTO("SECTIONS", "Sections", s.isSectionsCompleted(), true),
                new WorkspaceStepDTO("TEACHERS", "Teachers", s.isTeachersCompleted(), true),
                new WorkspaceStepDTO("SUBJECTS", "Subjects", s.isSubjectsCompleted(), true),
                new WorkspaceStepDTO("HOLIDAY_CALENDAR", "Holiday Calendar", s.isHolidayCalendarCompleted(), true)
        ));
        return dto;
    }

    private boolean notBlank(String value) { return value != null && !value.isBlank(); }
    private String clean(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim(); }
}
