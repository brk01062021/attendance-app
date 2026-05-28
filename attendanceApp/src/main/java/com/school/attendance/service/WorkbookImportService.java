package com.school.attendance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.dto.imports.*;
import com.school.attendance.entity.SchoolImportUpload;
import com.school.attendance.repository.SchoolImportUploadRepository;
import com.school.attendance.tenant.TenantContext;
import com.school.attendance.tenant.TenantUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkbookImportService {

    private final ImportValidationService importValidationService;
    private final SchoolImportUploadRepository uploadRepository;
    private final ObjectMapper objectMapper;

    public WorkbookImportService(ImportValidationService importValidationService,
                                 SchoolImportUploadRepository uploadRepository,
                                 ObjectMapper objectMapper) {
        this.importValidationService = importValidationService;
        this.uploadRepository = uploadRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportUploadResponseDTO uploadWorkbook(MultipartFile file,
                                                  String schoolId,
                                                  String academicYear,
                                                  String importType,
                                                  String requestedByRole) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload a valid Excel workbook before validation.");
        }
        String normalizedSchoolId = resolveSchoolId(schoolId);
        String safeImportType = normalizeImportType(importType);
        String safeRole = blankToDefault(requestedByRole, "ADMIN").toUpperCase(Locale.ROOT);
        String safeYear = blankToDefault(academicYear, "2026-2027");
        String fileName = blankToDefault(file.getOriginalFilename(), "school-import.xlsx");
        validateWorkbookFileName(fileName);

        try {
            byte[] bytes = file.getBytes();
            String checksum = sha256(bytes);
            String importBatchId = buildImportBatchId(normalizedSchoolId, checksum);
            WorkbookSnapshot snapshot = parseWorkbook(bytes);

            ImportValidationRequestDTO request = new ImportValidationRequestDTO();
            request.setSchoolId(normalizedSchoolId);
            request.setImportType(safeImportType);
            request.setFileName(fileName);
            request.setRequestedByRole(safeRole);
            request.setSheets(snapshot.sheets());
            ImportPreviewResponseDTO preview = importValidationService.validatePreview(request);
            hydrateWorkbookDataIssues(snapshot, normalizedSchoolId, preview);

            boolean duplicate = uploadRepository
                    .findFirstBySchoolCodeIgnoreCaseAndChecksumAndRolledBackFalseOrderByUploadedAtDesc(normalizedSchoolId, checksum)
                    .isPresent();
            if (duplicate) {
                preview.getIssues().add(new ImportValidationIssueDTO("Workbook", 0, "file", "WARNING", "This workbook was uploaded earlier for the same school_id. Review before committing again."));
            }
            refreshPreviewStatus(preview);

            SchoolImportUpload upload = new SchoolImportUpload();
            upload.setSchoolCode(normalizedSchoolId);
            upload.setAcademicYear(safeYear);
            upload.setImportType(safeImportType);
            upload.setFileName(fileName);
            upload.setChecksum(checksum);
            upload.setStatus(preview.getStatus());
            upload.setImportBatchId(importBatchId);
            upload.setTotalRows(preview.getRowCounts().values().stream().mapToInt(Integer::intValue).sum());
            upload.setTotalSheets(preview.getRowCounts().size());
            upload.setErrorCount((int) preview.getIssues().stream().filter(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity())).count());
            upload.setWarningCount((int) preview.getIssues().stream().filter(issue -> "WARNING".equalsIgnoreCase(issue.getSeverity())).count());
            upload.setUploadedByRole(safeRole);
            upload.setPreviewJson(writePreview(preview));
            uploadRepository.save(upload);

            ImportUploadResponseDTO response = new ImportUploadResponseDTO();
            response.setUploadId(upload.getId());
            response.setSchoolId(normalizedSchoolId);
            response.setAcademicYear(safeYear);
            response.setImportType(safeImportType);
            response.setFileName(fileName);
            response.setChecksum(checksum);
            response.setStatus(preview.getStatus());
            response.setImportBatchId(importBatchId);
            response.setDuplicateFile(duplicate);
            response.setPreview(preview);
            response.setUploadedAt(upload.getUploadedAt());
            return response;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read uploaded workbook. Please upload a valid .xlsx file.");
        }
    }

    public List<ImportUploadHistoryDTO> history(String schoolId) {
        String normalizedSchoolId = resolveSchoolId(schoolId);
        return uploadRepository.findTop20BySchoolCodeIgnoreCaseOrderByUploadedAtDesc(normalizedSchoolId)
                .stream()
                .map(this::toHistory)
                .collect(Collectors.toList());
    }

    public ImportPreviewResponseDTO preview(Long uploadId, String schoolId) {
        SchoolImportUpload upload = tenantUpload(uploadId, schoolId);
        try {
            return objectMapper.readValue(upload.getPreviewJson(), ImportPreviewResponseDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Import preview is not readable. Please upload the workbook again.");
        }
    }

    @Transactional
    public ImportCommitResponseDTO commit(Long uploadId, String schoolId) {
        SchoolImportUpload upload = tenantUpload(uploadId, schoolId);
        if (upload.isRolledBack()) {
            throw new IllegalStateException("This import was rolled back and cannot be committed.");
        }
        if (upload.getErrorCount() > 0) {
            throw new IllegalStateException("Resolve workbook validation errors before committing import data.");
        }
        upload.setCommitted(true);
        upload.setStatus("PENDING_PRINCIPAL_APPROVAL");
        upload.setCommittedAt(LocalDateTime.now());
        uploadRepository.save(upload);
        return action(upload, "Import committed into staging and is ready for principal onboarding approval.");
    }

    @Transactional
    public ImportCommitResponseDTO rollback(Long uploadId, String schoolId) {
        SchoolImportUpload upload = tenantUpload(uploadId, schoolId);
        upload.setRolledBack(true);
        upload.setCommitted(false);
        upload.setStatus("ROLLED_BACK");
        upload.setRolledBackAt(LocalDateTime.now());
        uploadRepository.save(upload);
        return action(upload, "Import rolled back from active onboarding history.");
    }

    private WorkbookSnapshot parseWorkbook(byte[] bytes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(bytes))) {
            List<ImportSheetPreviewDTO> sheets = new ArrayList<>();
            Map<String, List<RowSnapshot>> rowsBySheet = new LinkedHashMap<>();

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Row header = sheet.getRow(sheet.getFirstRowNum());
                List<String> headers = new ArrayList<>();
                if (header != null) {
                    for (Cell cell : header) {
                        String value = cellValue(cell);
                        if (!value.isBlank()) headers.add(value);
                    }
                }

                List<RowSnapshot> dataRows = new ArrayList<>();
                for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null || isBlankRow(row)) continue;

                    Map<String, String> values = new LinkedHashMap<>();
                    for (int c = 0; c < headers.size(); c++) {
                        String key = normalizeHeader(headers.get(c));
                        values.put(key, cellValue(row.getCell(c)));
                    }
                    dataRows.add(new RowSnapshot(r + 1, values));
                }

                sheets.add(new ImportSheetPreviewDTO(sheet.getSheetName(), dataRows.size(), headers));
                rowsBySheet.put(sheet.getSheetName(), dataRows);
            }
            return new WorkbookSnapshot(sheets, rowsBySheet);
        }
    }

    private void hydrateWorkbookDataIssues(WorkbookSnapshot snapshot, String activeSchoolId, ImportPreviewResponseDTO preview) {
        List<ImportValidationIssueDTO> issues = preview.getIssues();
        List<RowSnapshot> schoolRows = snapshot.rows("SchoolProfile");
        if (!schoolRows.isEmpty()) {
            String workbookSchoolId = schoolRows.get(0).value("school_id");
            if (!workbookSchoolId.isBlank() && !activeSchoolId.equalsIgnoreCase(TenantUtils.normalize(workbookSchoolId))) {
                issues.add(new ImportValidationIssueDTO("SchoolProfile", schoolRows.get(0).rowNumber(), "school_id", "ERROR", "Workbook school_id does not match the active tenant school_id."));
            }
        }

        Set<String> studentAdmissionNumbers = new HashSet<>();
        Set<String> duplicateAdmissions = new HashSet<>();
        for (RowSnapshot row : snapshot.rows("Students")) {
            String admissionNo = row.value("admission_no");
            if (admissionNo.isBlank()) {
                issues.add(new ImportValidationIssueDTO("Students", row.rowNumber(), "admission_no", "ERROR", "Student admission number is required."));
                continue;
            }
            String normalizedAdmission = admissionNo.toUpperCase(Locale.ROOT);
            if (!studentAdmissionNumbers.add(normalizedAdmission) && duplicateAdmissions.add(normalizedAdmission)) {
                issues.add(new ImportValidationIssueDTO("Students", row.rowNumber(), "admission_no", "ERROR", "Duplicate student admission number found in workbook."));
            }
            if (row.value("student_name").isBlank()) {
                issues.add(new ImportValidationIssueDTO("Students", row.rowNumber(), "student_name", "WARNING", "Student name is missing."));
            }
        }

        for (RowSnapshot row : snapshot.rows("Parents")) {
            String admissionNo = row.value("admission_no");
            if (admissionNo.isBlank()) {
                issues.add(new ImportValidationIssueDTO("Parents", row.rowNumber(), "admission_no", "ERROR", "Parent row must include a student admission number."));
                continue;
            }
            if (!studentAdmissionNumbers.contains(admissionNo.toUpperCase(Locale.ROOT))) {
                issues.add(new ImportValidationIssueDTO("Parents", row.rowNumber(), "admission_no", "ERROR", "Parent record does not match any student admission number in this workbook."));
            }
            if (row.value("mobile").isBlank()) {
                issues.add(new ImportValidationIssueDTO("Parents", row.rowNumber(), "mobile", "WARNING", "Parent mobile number is missing."));
            }
        }

        Set<String> teacherIds = new HashSet<>();
        Set<String> duplicateTeachers = new HashSet<>();
        for (RowSnapshot row : snapshot.rows("Teachers")) {
            String teacherId = row.value("teacher_id");
            if (teacherId.isBlank()) {
                issues.add(new ImportValidationIssueDTO("Teachers", row.rowNumber(), "teacher_id", "ERROR", "Teacher ID is required."));
                continue;
            }
            String normalizedTeacher = teacherId.toUpperCase(Locale.ROOT);
            if (!teacherIds.add(normalizedTeacher) && duplicateTeachers.add(normalizedTeacher)) {
                issues.add(new ImportValidationIssueDTO("Teachers", row.rowNumber(), "teacher_id", "ERROR", "Duplicate teacher ID found in workbook."));
            }
        }

        Set<String> subjects = snapshot.rows("Subjects").stream()
                .map(row -> row.value("subject_name").toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
        Set<String> classSections = snapshot.rows("ClassSections").stream()
                .map(row -> row.value("class_name").toLowerCase(Locale.ROOT) + "|" + row.value("section").toLowerCase(Locale.ROOT))
                .filter(value -> !"|".equals(value))
                .collect(Collectors.toSet());

        for (RowSnapshot row : snapshot.rows("TeacherAssignments")) {
            String teacherId = row.value("teacher_id");
            String subject = row.value("subject");
            String classSection = row.value("class_name").toLowerCase(Locale.ROOT) + "|" + row.value("section").toLowerCase(Locale.ROOT);
            if (!teacherId.isBlank() && !teacherIds.contains(teacherId.toUpperCase(Locale.ROOT))) {
                issues.add(new ImportValidationIssueDTO("TeacherAssignments", row.rowNumber(), "teacher_id", "ERROR", "Teacher assignment references a teacher ID not found in Teachers sheet."));
            }
            if (!subject.isBlank() && !subjects.isEmpty() && !subjects.contains(subject.toLowerCase(Locale.ROOT))) {
                issues.add(new ImportValidationIssueDTO("TeacherAssignments", row.rowNumber(), "subject", "WARNING", "Teacher assignment subject is not listed in Subjects sheet."));
            }
            if (!classSections.isEmpty() && !classSections.contains(classSection)) {
                issues.add(new ImportValidationIssueDTO("TeacherAssignments", row.rowNumber(), "class_name/section", "WARNING", "Teacher assignment class-section is not listed in ClassSections sheet."));
            }
        }
    }

    private void refreshPreviewStatus(ImportPreviewResponseDTO preview) {
        boolean hasErrors = preview.getIssues().stream().anyMatch(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity()));
        boolean hasWarnings = preview.getIssues().stream().anyMatch(issue -> "WARNING".equalsIgnoreCase(issue.getSeverity()));
        preview.setValid(!hasErrors);
        preview.setTenantSafe(!hasErrors && TenantUtils.isValidSchoolId(preview.getSchoolId()));
        preview.setStatus(hasErrors ? "BLOCKED" : hasWarnings ? "READY_WITH_WARNINGS" : "READY_TO_IMPORT");
        preview.setSummary(buildSummary(preview));
    }

    private String buildSummary(ImportPreviewResponseDTO preview) {
        int totalRows = preview.getRowCounts().values().stream().mapToInt(Integer::intValue).sum();
        long errors = preview.getIssues().stream().filter(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity())).count();
        long warnings = preview.getIssues().stream().filter(issue -> "WARNING".equalsIgnoreCase(issue.getSeverity())).count();
        return "Preview checked " + preview.getRowCounts().size() + " sheet(s), " + totalRows + " row(s), " + errors + " error(s), " + warnings + " warning(s).";
    }

    private boolean isBlankRow(Row row) {
        for (Cell cell : row) {
            if (!cellValue(cell).isBlank()) return false;
        }
        return true;
    }

    private String cellValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(bytes);
            StringBuilder hex = new StringBuilder();
            for (byte b : encodedHash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Checksum generation is unavailable.");
        }
    }

    private SchoolImportUpload tenantUpload(Long uploadId, String schoolId) {
        String normalizedSchoolId = resolveSchoolId(schoolId);
        SchoolImportUpload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Import upload was not found."));
        if (!normalizedSchoolId.equalsIgnoreCase(upload.getSchoolCode())) {
            throw new IllegalArgumentException("Import upload does not belong to the active school_id.");
        }
        return upload;
    }

    private ImportUploadHistoryDTO toHistory(SchoolImportUpload upload) {
        ImportUploadHistoryDTO dto = new ImportUploadHistoryDTO();
        dto.setUploadId(upload.getId());
        dto.setSchoolId(upload.getSchoolCode());
        dto.setFileName(upload.getFileName());
        dto.setImportType(upload.getImportType());
        dto.setAcademicYear(upload.getAcademicYear());
        dto.setStatus(upload.getStatus());
        dto.setImportBatchId(upload.getImportBatchId());
        dto.setTotalRows(upload.getTotalRows());
        dto.setTotalSheets(upload.getTotalSheets());
        dto.setErrorCount(upload.getErrorCount());
        dto.setWarningCount(upload.getWarningCount());
        dto.setCommitted(upload.isCommitted());
        dto.setRolledBack(upload.isRolledBack());
        dto.setUploadedAt(upload.getUploadedAt());
        return dto;
    }

    private ImportCommitResponseDTO action(SchoolImportUpload upload, String message) {
        ImportCommitResponseDTO dto = new ImportCommitResponseDTO();
        dto.setUploadId(upload.getId());
        dto.setSchoolId(upload.getSchoolCode());
        dto.setStatus(upload.getStatus());
        dto.setImportBatchId(upload.getImportBatchId());
        dto.setCommitted(upload.isCommitted());
        dto.setRolledBack(upload.isRolledBack());
        dto.setMessage(message);
        dto.setActionAt(LocalDateTime.now());
        return dto;
    }

    private String buildImportBatchId(String schoolId, String checksum) {
        String suffix = checksum == null || checksum.length() < 8 ? UUID.randomUUID().toString().substring(0, 8) : checksum.substring(0, 8);
        return schoolId.toUpperCase(Locale.ROOT) + "-IMP-" + suffix.toUpperCase(Locale.ROOT);
    }

    private String writePreview(ImportPreviewResponseDTO preview) {
        try {
            return objectMapper.writeValueAsString(preview);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to store import preview.");
        }
    }

    private String resolveSchoolId(String schoolId) {
        String requested = TenantUtils.normalize(schoolId);
        String context = TenantContext.getSchoolId();
        boolean requestedValid = TenantUtils.isValidSchoolId(requested);
        boolean contextValid = TenantUtils.isValidSchoolId(context);
        boolean contextIsDefault = TenantContext.DEFAULT_SCHOOL_ID.equalsIgnoreCase(context);

        String resolved = requestedValid ? requested : context;
        if (!TenantUtils.isValidSchoolId(resolved)) {
            throw new IllegalArgumentException("school_id must be exactly 4 uppercase alphanumeric characters.");
        }

        // Browser multipart uploads may arrive with schoolId in form-data before the tenant
        // filter can reliably bind a non-default tenant. Treat DEMO as an unset fallback
        // and enforce mismatch only when both sides carry real, non-default tenant ids.
        if (requestedValid && contextValid && !contextIsDefault && !requested.equalsIgnoreCase(context)) {
            throw new IllegalArgumentException("Request school_id does not match active tenant context.");
        }
        return resolved;
    }

    private void validateWorkbookFileName(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx onboarding workbooks are supported for backend validation.");
        }
    }

    private String normalizeImportType(String value) {
        return blankToDefault(value, "MASTER_WORKBOOK")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace("+", "AND");
    }

    private String normalizeHeader(String value) {
        return blankToDefault(value, "")
                .toLowerCase(Locale.ROOT)
                .replace(" ", "_")
                .replace("-", "_");
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private record WorkbookSnapshot(List<ImportSheetPreviewDTO> sheets, Map<String, List<RowSnapshot>> rowsBySheet) {
        List<RowSnapshot> rows(String sheetName) {
            return rowsBySheet.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(sheetName))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(List.of());
        }
    }

    private record RowSnapshot(int rowNumber, Map<String, String> values) {
        String value(String key) {
            return values.getOrDefault(key, "").trim();
        }
    }
}
