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
                new ImportSheetPreviewDTO("TeacherPools", 10, List.of("class_name")),
                new ImportSheetPreviewDTO("Schedules", 40, List.of("period"))
        ));

        ImportPreviewResponseDTO response = service.validatePreview(request);

        assertTrue(response.isValid());
        assertTrue(response.isTenantSafe());
    }
}
