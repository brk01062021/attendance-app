package com.school.attendance.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.dto.imports.*;
import com.school.attendance.service.ImportValidationService;
import com.school.attendance.service.WorkbookImportService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/imports", "/api/import-validation"})
public class ImportValidationController {

    private final ImportValidationService importValidationService;
    private final WorkbookImportService workbookImportService;

    public ImportValidationController(ImportValidationService importValidationService,
                                      WorkbookImportService workbookImportService) {
        this.importValidationService = importValidationService;
        this.workbookImportService = workbookImportService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("service", "excel-import-validation");
        data.put("status", "UP");
        data.put("features", List.of("xlsx-upload", "preview", "history", "commit", "rollback", "tenant-isolation"));
        data.put("timestamp", Instant.now().toString());
        return ApiResponse.success("Import validation engine is ready", data);
    }

    @GetMapping("/template-rules")
    public ApiResponse<Map<String, Object>> templateRules(@RequestParam(defaultValue = "ADMIN") String role) {
        String normalizedRole = role == null || role.isBlank() ? "ADMIN" : role.trim().toUpperCase();

        List<String> requiredSheets = List.of(
                "SchoolProfile", "Students", "Parents", "Teachers", "TeacherAssignments",
                "Subjects", "ClassSections", "TeacherPools", "Schedules"
        );

        Map<String, List<String>> requiredColumns = new LinkedHashMap<>();
        requiredColumns.put("SchoolProfile", List.of("school_id", "school_name", "academic_year"));
        requiredColumns.put("Students", List.of("admission_no", "student_name", "class_name", "section"));
        requiredColumns.put("Parents", List.of("admission_no", "parent_name", "mobile"));
        requiredColumns.put("Teachers", List.of("teacher_id", "teacher_name", "mobile"));
        requiredColumns.put("TeacherAssignments", List.of("teacher_id", "class_name", "section", "subject"));
        requiredColumns.put("Subjects", List.of("subject_name", "subject_type", "weekly_periods"));
        requiredColumns.put("ClassSections", List.of("class_name", "section"));
        requiredColumns.put("TeacherPools", List.of("class_name", "teacher_pool"));
        requiredColumns.put("Schedules", List.of("day", "period", "start_time", "end_time"));

        List<String> validationRules = new ArrayList<>();
        validationRules.add("school_id must be a 4-character uppercase alphanumeric tenant identifier.");
        validationRules.add("Admission numbers must be unique inside one school_id.");
        validationRules.add("Parent rows must link to a valid student admission_no.");
        validationRules.add("Teacher assignments must reference valid teacher_id, class_name, section, and subject values.");
        validationRules.add("Workbook preview can show warnings, but commit is blocked for tenant mismatch or missing required sheets.");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role", normalizedRole);
        data.put("allowedRoles", List.of("ADMIN", "PRINCIPAL"));
        data.put("importType", "MASTER_WORKBOOK");
        data.put("requiredSheets", requiredSheets);
        data.put("requiredColumns", requiredColumns);
        data.put("validationRules", validationRules);
        data.put("status", List.of("READY_TO_IMPORT", "READY_WITH_WARNINGS", "BLOCKED", "COMMITTED", "ROLLED_BACK"));
        data.put("generatedAt", Instant.now().toString());

        return ApiResponse.success("Import template rules loaded", data);
    }

    @PostMapping("/preview/validate")
    public ApiResponse<ImportPreviewResponseDTO> validatePreview(@RequestBody ImportValidationRequestDTO request) {
        ImportPreviewResponseDTO response = importValidationService.validatePreview(request);
        return ApiResponse.success("Excel import preview validation completed", response);
    }

    @PostMapping("/workbooks/upload")
    public ApiResponse<ImportUploadResponseDTO> uploadWorkbook(@RequestParam("file") MultipartFile file,
                                                               @RequestParam String schoolId,
                                                               @RequestParam(defaultValue = "2026-2027") String academicYear,
                                                               @RequestParam(defaultValue = "MASTER_WORKBOOK") String importType,
                                                               @RequestParam(defaultValue = "ADMIN") String requestedByRole) {
        ImportUploadResponseDTO response = workbookImportService.uploadWorkbook(file, schoolId, academicYear, importType, requestedByRole);
        return ApiResponse.success("Workbook uploaded and validated", response);
    }

    @GetMapping({"/workbooks/history", "/upload-history"})
    public ApiResponse<List<ImportUploadHistoryDTO>> history(@RequestParam String schoolId) {
        return ApiResponse.success("Workbook upload history loaded", workbookImportService.history(schoolId));
    }

    @GetMapping("/workbooks/{uploadId}/preview")
    public ApiResponse<ImportPreviewResponseDTO> preview(@PathVariable Long uploadId, @RequestParam String schoolId) {
        return ApiResponse.success("Workbook preview loaded", workbookImportService.preview(uploadId, schoolId));
    }

    @PostMapping("/workbooks/{uploadId}/commit")
    public ApiResponse<ImportCommitResponseDTO> commit(@PathVariable Long uploadId, @RequestParam String schoolId) {
        return ApiResponse.success("Workbook import committed", workbookImportService.commit(uploadId, schoolId));
    }

    @PostMapping("/workbooks/{uploadId}/rollback")
    public ApiResponse<ImportCommitResponseDTO> rollback(@PathVariable Long uploadId, @RequestParam String schoolId) {
        return ApiResponse.success("Workbook import rolled back", workbookImportService.rollback(uploadId, schoolId));
    }
}
