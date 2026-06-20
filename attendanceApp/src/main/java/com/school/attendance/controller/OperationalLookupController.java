package com.school.attendance.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.dto.StudentSearchDTO;
import com.school.attendance.dto.TeacherSearchDTO;
import com.school.attendance.service.OperationalLookupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping({"/api/operational-lookups", "/operational"})
public class OperationalLookupController {

    private static final Pattern SCHOOL_ID_PATTERN = Pattern.compile("^[A-Z0-9]{4}$");

    private final OperationalLookupService lookupService;

    public OperationalLookupController(OperationalLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/academic-years")
    public ApiResponse<List<String>> academicYears(@RequestParam(required = false) String schoolId) {
        return ApiResponse.success("Academic years loaded", lookupService.academicYears());
    }

    @GetMapping("/months")
    public ApiResponse<List<String>> months(@RequestParam String schoolId) {
        validateSchoolId(schoolId);
        return ApiResponse.success("Academic months loaded", lookupService.months(schoolId));
    }

    @GetMapping("/classes")
    public ApiResponse<List<String>> classes(@RequestParam String schoolId) {
        validateSchoolId(schoolId);
        return ApiResponse.success("Classes loaded", lookupService.classes(schoolId));
    }

    @GetMapping("/sections")
    public ApiResponse<List<String>> sections(@RequestParam String schoolId,
                                              @RequestParam(required = false, defaultValue = "") String className) {
        validateSchoolId(schoolId);
        return ApiResponse.success("Sections loaded", lookupService.sections(schoolId, className));
    }

    @GetMapping("/subjects")
    public ApiResponse<List<String>> subjects(@RequestParam String schoolId) {
        validateSchoolId(schoolId);
        return ApiResponse.success("Subjects loaded", lookupService.subjects(schoolId));
    }

    @GetMapping("/students/search")
    public ApiResponse<List<StudentSearchDTO>> students(@RequestParam String schoolId,
                                                        @RequestParam(required = false, defaultValue = "") String query) {
        validateSchoolId(schoolId);
        return ApiResponse.success("Students loaded", lookupService.students(schoolId, query));
    }

    @GetMapping("/teachers/search")
    public ApiResponse<List<TeacherSearchDTO>> teachers(@RequestParam String schoolId,
                                                        @RequestParam(required = false, defaultValue = "") String query) {
        validateSchoolId(schoolId);
        return ApiResponse.success("Teachers loaded", lookupService.teachers(schoolId, query));
    }

    @GetMapping("/tenant/validate")
    public ApiResponse<Map<String, Object>> tenant(@RequestParam String schoolId) {
        String normalized = normalizeSchoolId(schoolId);
        return ApiResponse.success("school_id validation completed", Map.of(
                "schoolId", normalized,
                "valid", isValidSchoolId(normalized),
                "rule", "Exactly 4 uppercase alphanumeric characters"
        ));
    }

    private void validateSchoolId(String schoolId) {
        String normalized = normalizeSchoolId(schoolId);
        if (!isValidSchoolId(normalized)) {
            throw new IllegalArgumentException("school_id must be exactly 4 uppercase alphanumeric characters.");
        }
    }

    private boolean isValidSchoolId(String schoolId) {
        return schoolId != null && SCHOOL_ID_PATTERN.matcher(schoolId).matches();
    }

    private String normalizeSchoolId(String schoolId) {
        return schoolId == null ? "" : schoolId.trim().toUpperCase();
    }
}
