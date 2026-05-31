package com.school.attendance.service;

import com.school.attendance.dto.imports.ImportPreviewResponseDTO;
import com.school.attendance.dto.imports.ImportSheetPreviewDTO;
import com.school.attendance.dto.imports.ImportValidationRequestDTO;
import com.school.attendance.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportValidationServiceTest {

    private final ImportValidationService service = new ImportValidationService();

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void blocksInvalidSchoolId() {
        TenantContext.setSchoolId("DEMO");
        ImportValidationRequestDTO request = new ImportValidationRequestDTO();
        request.setSchoolId("12345");
        request.setRequestedByRole("ADMIN");

        ImportPreviewResponseDTO response = service.validatePreview(request);

        assertFalse(response.isValid());
        assertTrue(response.getIssues().stream().anyMatch(issue -> "school_id".equals(issue.getFieldName())));
    }

    @Test
    void allowsAdminWithAllRequiredSheets() {
        TenantContext.setSchoolId("AB12");
        ImportValidationRequestDTO request = new ImportValidationRequestDTO();
        request.setSchoolId("AB12");
        request.setRequestedByRole("ADMIN");
        request.setSheets(List.of(
                new ImportSheetPreviewDTO("SchoolProfile", 1, List.of("school_id")),
                new ImportSheetPreviewDTO("Students", 300, List.of("admission_no")),
                new ImportSheetPreviewDTO("Parents", 300, List.of("parent_name")),
                new ImportSheetPreviewDTO("Teachers", 30, List.of("teacher_name")),
                new ImportSheetPreviewDTO("TeacherAssignments", 90, List.of("teacher_id")),
                new ImportSheetPreviewDTO("Subjects", 20, List.of("subject_name")),
                new ImportSheetPreviewDTO("ClassSections", 20, List.of("class_name")),
                new ImportSheetPreviewDTO("TeacherPools", 10, List.of("class_name", "teacher_pool")),
                new ImportSheetPreviewDTO("AcademicRules", 20, List.of("subject_name", "subject_type", "weekly_periods")),
                new ImportSheetPreviewDTO("Schedules", 40, List.of("day", "period", "start_time", "end_time"))
        ));

        ImportPreviewResponseDTO response = service.validatePreview(request);

        assertTrue(response.isValid());
        assertTrue(response.isTenantSafe());
    }
    @Test
    void blocksWhenAcademicRulesSheetMissingForTimetableReadiness() {
        TenantContext.setSchoolId("AB12");
        ImportValidationRequestDTO request = new ImportValidationRequestDTO();
        request.setSchoolId("AB12");
        request.setRequestedByRole("PRINCIPAL");
        request.setSheets(List.of(
                new ImportSheetPreviewDTO("SchoolProfile", 1, List.of("school_id", "school_name", "academic_year")),
                new ImportSheetPreviewDTO("Students", 10, List.of("admission_no", "student_name", "class_name", "section")),
                new ImportSheetPreviewDTO("Parents", 10, List.of("admission_no", "parent_name", "mobile")),
                new ImportSheetPreviewDTO("Teachers", 3, List.of("teacher_id", "teacher_name", "mobile")),
                new ImportSheetPreviewDTO("TeacherAssignments", 6, List.of("teacher_id", "class_name", "section", "subject")),
                new ImportSheetPreviewDTO("Subjects", 5, List.of("subject_name", "subject_type", "weekly_periods")),
                new ImportSheetPreviewDTO("ClassSections", 2, List.of("class_name", "section")),
                new ImportSheetPreviewDTO("TeacherPools", 2, List.of("class_name", "teacher_pool")),
                new ImportSheetPreviewDTO("Schedules", 7, List.of("day", "period", "start_time", "end_time"))
        ));

        ImportPreviewResponseDTO response = service.validatePreview(request);

        assertFalse(response.isValid());
        assertTrue(response.getIssues().stream().anyMatch(issue -> "AcademicRules".equals(issue.getSheetName())));
    }

}
