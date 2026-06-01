package com.school.attendance.service;

import com.school.attendance.dto.imports.ImportPreviewResponseDTO;
import com.school.attendance.dto.imports.ImportSheetPreviewDTO;
import com.school.attendance.dto.imports.ImportValidationIssueDTO;
import com.school.attendance.dto.imports.ImportValidationRequestDTO;
import com.school.attendance.dto.imports.WorkbookErrorGroupDTO;
import com.school.attendance.dto.imports.WorkbookErrorIntelligenceDTO;
import com.school.attendance.tenant.TenantContext;
import com.school.attendance.tenant.TenantUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ImportValidationService {

    private static final Set<String> ADMIN_IMPORT_ROLES = Set.of("ADMIN", "PRINCIPAL");
    private static final List<String> REQUIRED_MASTER_SHEETS = List.of(
            "SchoolProfile", "Students", "Parents", "Teachers", "TeacherAssignments",
            "Subjects", "ClassSections", "TeacherPools", "AcademicRules", "Schedules"
    );

    public ImportPreviewResponseDTO validatePreview(ImportValidationRequestDTO request) {
        ImportValidationRequestDTO safeRequest = request == null ? new ImportValidationRequestDTO() : request;
        String requestSchoolId = TenantUtils.normalize(safeRequest.getSchoolId());
        String contextSchoolId = TenantContext.getSchoolId();
        String schoolId = TenantUtils.isValidSchoolId(requestSchoolId) ? requestSchoolId : contextSchoolId;
        String role = normalizeRole(safeRequest.getRequestedByRole());

        List<ImportValidationIssueDTO> issues = new ArrayList<>();
        validateTenant(schoolId, contextSchoolId, requestSchoolId, issues);
        validateRole(role, issues);

        String importType = blankToDefault(safeRequest.getImportType(), "MASTER_WORKBOOK").toUpperCase(Locale.ROOT);
        List<ImportSheetPreviewDTO> sheets = safeRequest.getSheets() == null ? List.of() : safeRequest.getSheets();
        Map<String, Integer> rowCounts = new LinkedHashMap<>();

        for (ImportSheetPreviewDTO sheet : sheets) {
            String sheetName = blankToDefault(sheet.getSheetName(), "UNKNOWN");
            int totalRows = Math.max(0, sheet.getTotalRows());
            rowCounts.put(sheetName, totalRows);
            if (totalRows == 0 && !"SchoolProfile".equalsIgnoreCase(sheetName)) {
                issues.add(new ImportValidationIssueDTO(sheetName, 1, "rows", "WARNING", "Sheet has no data rows. Confirm this is expected before import."));
            }
            validateSheetHeaders(sheetName, sheet.getHeaders(), issues);
        }

        if (sheets.isEmpty()) {
            issues.add(new ImportValidationIssueDTO("Workbook", 0, "sheets", "ERROR", "No workbook sheets were supplied for preview validation."));
        }

        validateRequiredSheets(sheets, issues);

        boolean hasErrors = issues.stream().anyMatch(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity()));
        ImportPreviewResponseDTO response = new ImportPreviewResponseDTO();
        response.setSchoolId(schoolId);
        response.setImportType(importType);
        response.setFileName(blankToDefault(safeRequest.getFileName(), "pending-upload.xlsx"));
        response.setTenantSafe(!hasErrors && TenantUtils.isValidSchoolId(schoolId));
        response.setValid(!hasErrors);
        response.setStatus(hasErrors ? "BLOCKED" : issues.isEmpty() ? "READY_TO_IMPORT" : "READY_WITH_WARNINGS");
        response.setSummary(buildSummary(rowCounts, issues));
        response.setRowCounts(rowCounts);
        response.setPreviewSheets(new ArrayList<>(sheets));
        response.setIssues(issues);
        response.setErrorIntelligence(buildErrorIntelligence(response));
        return response;
    }

    private void validateTenant(String schoolId, String contextSchoolId, String requestSchoolId, List<ImportValidationIssueDTO> issues) {
        if (requestSchoolId != null && !TenantUtils.isValidSchoolId(requestSchoolId)) {
            issues.add(new ImportValidationIssueDTO("Workbook", 0, "school_id", "ERROR", "school_id must be exactly 4 uppercase alphanumeric characters."));
            return;
        }
        if (!TenantUtils.isValidSchoolId(schoolId)) {
            issues.add(new ImportValidationIssueDTO("Workbook", 0, "school_id", "ERROR", "school_id must be exactly 4 uppercase alphanumeric characters."));
            return;
        }

        boolean requestValid = TenantUtils.isValidSchoolId(requestSchoolId);
        boolean contextValid = TenantUtils.isValidSchoolId(contextSchoolId);
        boolean contextIsDefault = TenantContext.DEFAULT_SCHOOL_ID.equalsIgnoreCase(contextSchoolId);
        if (requestValid && contextValid && !contextIsDefault && !requestSchoolId.equalsIgnoreCase(contextSchoolId)) {
            issues.add(new ImportValidationIssueDTO("Workbook", 0, "school_id", "ERROR", "Request school_id does not match active tenant context."));
        }
    }

    private void validateRole(String role, List<ImportValidationIssueDTO> issues) {
        if (!ADMIN_IMPORT_ROLES.contains(role)) {
            issues.add(new ImportValidationIssueDTO("Workbook", 0, "role", "ERROR", "Only ADMIN or PRINCIPAL can validate and process school imports."));
        }
    }

    private void validateRequiredSheets(List<ImportSheetPreviewDTO> sheets, List<ImportValidationIssueDTO> issues) {
        Set<String> supplied = sheets.stream()
                .map(ImportSheetPreviewDTO::getSheetName)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toSet());

        for (String requiredSheet : REQUIRED_MASTER_SHEETS) {
            if (!supplied.contains(requiredSheet)) {
                issues.add(new ImportValidationIssueDTO(requiredSheet, 0, "sheet", "ERROR", "Required sheet is missing from the onboarding workbook."));
            }
        }
    }


    private void validateSheetHeaders(String sheetName, List<String> headers, List<ImportValidationIssueDTO> issues) {
        Set<String> normalizedHeaders = headers == null ? Set.of() : headers.stream()
                                                                     .filter(name -> name != null && !name.isBlank())
                                                                     .map(name -> name.trim().toLowerCase(Locale.ROOT).replace(" ", "_").replace("-", "_"))
                                                                     .collect(java.util.stream.Collectors.toSet());

        Map<String, List<String>> requiredHeaders = Map.of(
                "Students", List.of("admission_no", "student_name", "class_name", "section"),
                "Parents", List.of("admission_no", "parent_name"),
                "Teachers", List.of("teacher_id", "teacher_name"),
                "TeacherAssignments", List.of("teacher_id", "class_name", "section", "subject"),
                "ClassSections", List.of("class_name", "section"),
                "TeacherPools", List.of("class_name", "teacher_pool"),
                "AcademicRules", List.of("subject_name", "subject_type", "weekly_periods"),
                "Schedules", List.of("day", "period", "start_time", "end_time")
        );

        List<String> required = requiredHeaders.get(sheetName);
        if (required == null) return;

        for (String header : required) {
            if (!normalizedHeaders.contains(header)) {
                issues.add(new ImportValidationIssueDTO(sheetName, 1, header, "WARNING", "Recommended column is missing for full import validation. Preview can continue, but commit validation may require this field."));
            }
        }
    }
    public WorkbookErrorIntelligenceDTO buildErrorIntelligence(ImportPreviewResponseDTO preview) {
        WorkbookErrorIntelligenceDTO intelligence = new WorkbookErrorIntelligenceDTO();
        if (preview == null) {
            intelligence.setStatus("NO_PREVIEW");
            intelligence.setHeadline("No workbook validation preview is available.");
            intelligence.setActivationBlocked(true);
            intelligence.getActivationBlockers().add("Upload and validate a workbook before activation review.");
            return intelligence;
        }
        List<ImportValidationIssueDTO> issues = preview.getIssues() == null ? List.of() : preview.getIssues();
        int errors = (int) issues.stream().filter(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity())).count();
        int warnings = (int) issues.stream().filter(issue -> "WARNING".equalsIgnoreCase(issue.getSeverity())).count();
        intelligence.setSchoolId(preview.getSchoolId());
        intelligence.setFileName(preview.getFileName());
        intelligence.setStatus(preview.getStatus());
        intelligence.setTotalErrors(errors);
        intelligence.setTotalWarnings(warnings);
        intelligence.setActivationBlocked(errors > 0 || !preview.isTenantSafe());
        intelligence.setHeadline(errors > 0
                ? "Activation is blocked until workbook errors are corrected."
                : warnings > 0 ? "Workbook can proceed after reviewing warnings." : "Workbook passed validation with no blocking issues.");

        Map<String, WorkbookErrorGroupDTO> groups = new LinkedHashMap<>();
        for (ImportValidationIssueDTO issue : issues) {
            String category = categorize(issue);
            WorkbookErrorGroupDTO group = groups.computeIfAbsent(category, this::newGroup);
            group.getIssues().add(issue);
            if ("ERROR".equalsIgnoreCase(issue.getSeverity())) group.setErrorCount(group.getErrorCount() + 1);
            if ("WARNING".equalsIgnoreCase(issue.getSeverity())) group.setWarningCount(group.getWarningCount() + 1);
            if ("MISSING_SHEETS".equals(category)) intelligence.getMissingSheets().add(issue.getSheetName());
            if ("SCHOOL_ID_MISMATCH".equals(category)) intelligence.getSchoolIdMismatchExplanations().add(issue.getMessage());
        }
        intelligence.setGroups(new ArrayList<>(groups.values()));
        if (!intelligence.getMissingSheets().isEmpty()) {
            intelligence.getActivationBlockers().add("Missing sheets: " + String.join(", ", intelligence.getMissingSheets()));
        }
        if (!intelligence.getSchoolIdMismatchExplanations().isEmpty()) {
            intelligence.getActivationBlockers().add("School ID mismatch must be fixed before commit or activation.");
        }
        groups.values().stream()
                .filter(group -> group.getErrorCount() > 0 && !"MISSING_SHEETS".equals(group.getCategory()) && !"SCHOOL_ID_MISMATCH".equals(group.getCategory()))
                .forEach(group -> intelligence.getActivationBlockers().add(group.getTitle() + " has " + group.getErrorCount() + " blocking issue(s)."));
        if (intelligence.getActivationBlockers().isEmpty() && warnings > 0) {
            intelligence.getActivationBlockers().add("No blocking errors. Review warnings before committing the workbook.");
        }
        if (intelligence.getActivationBlockers().isEmpty()) {
            intelligence.getActivationBlockers().add("No activation blockers detected from workbook validation.");
        }
        return intelligence;
    }

    private String categorize(ImportValidationIssueDTO issue) {
        String sheet = issue.getSheetName() == null ? "" : issue.getSheetName();
        String field = issue.getFieldName() == null ? "" : issue.getFieldName().toLowerCase(Locale.ROOT);
        String message = issue.getMessage() == null ? "" : issue.getMessage().toLowerCase(Locale.ROOT);
        if ("sheet".equals(field) && message.contains("missing")) return "MISSING_SHEETS";
        if (field.contains("school_id") || message.contains("tenant") || message.contains("school_id")) return "SCHOOL_ID_MISMATCH";
        if ("TeacherAssignments".equalsIgnoreCase(sheet) || "TeacherPools".equalsIgnoreCase(sheet)) return "TEACHER_ASSIGNMENT_ISSUES";
        if ("Schedules".equalsIgnoreCase(sheet) || "AcademicRules".equalsIgnoreCase(sheet) || field.contains("timetable_readiness")) return "SCHEDULE_ISSUES";
        if ("Students".equalsIgnoreCase(sheet) || "Parents".equalsIgnoreCase(sheet)) return "STUDENT_PARENT_LINKING";
        if ("Teachers".equalsIgnoreCase(sheet)) return "TEACHER_MASTER_DATA";
        return "GENERAL_WORKBOOK_ISSUES";
    }

    private WorkbookErrorGroupDTO newGroup(String category) {
        return switch (category) {
            case "MISSING_SHEETS" -> new WorkbookErrorGroupDTO(category, "Missing Sheets Summary", "One or more required workbook tabs are missing.", "Add the missing tabs using the VidyaSetu master workbook template and upload again.");
            case "SCHOOL_ID_MISMATCH" -> new WorkbookErrorGroupDTO(category, "School ID Mismatch", "The workbook school_id does not match the active tenant login context.", "Use the same 4-character school_id in SchoolProfile as the logged-in school workspace.");
            case "TEACHER_ASSIGNMENT_ISSUES" -> new WorkbookErrorGroupDTO(category, "Teacher Assignment Issues", "Teacher assignment or teacher pool rows are incomplete or reference unavailable teachers/classes/subjects.", "Verify Teachers, TeacherAssignments, TeacherPools, Subjects, and ClassSections tabs together.");
            case "SCHEDULE_ISSUES" -> new WorkbookErrorGroupDTO(category, "Schedule & Timetable Issues", "Schedule or academic rule data is missing or incomplete for timetable readiness.", "Complete Schedules, AcademicRules, Subjects, TeacherPools, and ClassSections before activation.");
            case "STUDENT_PARENT_LINKING" -> new WorkbookErrorGroupDTO(category, "Student / Parent Linking", "Student or parent rows need correction before reliable account linking.", "Ensure admission_no values are unique and parents reference valid students.");
            case "TEACHER_MASTER_DATA" -> new WorkbookErrorGroupDTO(category, "Teacher Master Data", "Teacher master rows need cleanup.", "Ensure each teacher_id is unique and teacher details are complete.");
            default -> new WorkbookErrorGroupDTO(category, "General Workbook Issues", "Workbook validation returned additional issues.", "Review the listed rows and upload the corrected workbook.");
        };
    }


    private String buildSummary(Map<String, Integer> rowCounts, List<ImportValidationIssueDTO> issues) {
        int totalRows = rowCounts.values().stream().mapToInt(Integer::intValue).sum();
        long errors = issues.stream().filter(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity())).count();
        long warnings = issues.stream().filter(issue -> "WARNING".equalsIgnoreCase(issue.getSeverity())).count();
        return "Preview checked " + rowCounts.size() + " sheet(s), " + totalRows + " row(s), " + errors + " error(s), " + warnings + " warning(s).";
    }

    private String normalizeRole(String role) {
        return blankToDefault(role, "ADMIN").trim().toUpperCase(Locale.ROOT);
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
