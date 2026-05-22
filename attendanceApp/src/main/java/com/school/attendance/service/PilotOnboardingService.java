package com.school.attendance.service;

import com.school.attendance.dto.PilotOnboardingStepDTO;
import com.school.attendance.dto.PilotOnboardingSummaryDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PilotOnboardingService {

    public PilotOnboardingSummaryDTO getPilotSummary(Long schoolId) {
        Long safeSchoolId = schoolId == null || schoolId <= 0 ? 1L : schoolId;

        List<PilotOnboardingStepDTO> steps = List.of(
                new PilotOnboardingStepDTO(
                        "TENANT_SETUP",
                        "Create pilot tenant and verify school_id isolation",
                        "Admin",
                        "READY_FOR_VALIDATION",
                        "P0",
                        "Confirm all Admin/Principal APIs pass school_id and no cross-school data is visible."
                ),
                new PilotOnboardingStepDTO(
                        "MASTER_IMPORT",
                        "Import school profile, classes, sections, students, parents, teachers, subjects, and teacher pools",
                        "Admin",
                        "IN_PROGRESS",
                        "P0",
                        "Use one Excel workbook per school and validate row-level errors before enabling live users."
                ),
                new PilotOnboardingStepDTO(
                        "TIMETABLE_DRAFT",
                        "Generate, review, repair, and publish first timetable batch",
                        "Principal",
                        "IN_PROGRESS",
                        "P0",
                        "Publish only after conflicts are zero or explicitly approved by Principal/Admin."
                ),
                new PilotOnboardingStepDTO(
                        "ATTENDANCE_TRIAL",
                        "Run teacher attendance trial for selected classes",
                        "Teacher Lead",
                        "PENDING",
                        "P1",
                        "Validate mobile-first daily attendance plus missed-day web import for teachers."
                ),
                new PilotOnboardingStepDTO(
                        "HOLIDAY_NOTICE",
                        "Configure holiday calendar overrides and notice preview",
                        "Principal",
                        "PENDING",
                        "P1",
                        "Full-day and half-day holidays should lock attendance without changing original timetable."
                ),
                new PilotOnboardingStepDTO(
                        "GO_LIVE_SIGNOFF",
                        "Pilot go-live signoff for one realistic school",
                        "School Head",
                        "PENDING",
                        "P0",
                        "Enable parents/students only after Admin, Principal, and teachers validate real data."
                )
        );

        return new PilotOnboardingSummaryDTO(
                safeSchoolId,
                "VidyaSetu Pilot School",
                700,
                40,
                1,
                1,
                "PILOT_PREPARATION",
                LocalDate.now().plusDays(14),
                steps
        );
    }
}
