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
        String safeImportType = blankToDefault(importType, "MASTER_WORKBOOK").toUpperCase(Locale.ROOT).replace(' ', '_');
        String safeRole = blankToDefault(requestedByRole, "ADMIN").toUpperCase(Locale.ROOT);
        String safeYear = blankToDefault(academicYear, "2026-2027");
        String fileName = blankToDefault(file.getOriginalFilename(), "school-import.xlsx");

        try {
            byte[] bytes = file.getBytes();
            String checksum = sha256(bytes);
            List<ImportSheetPreviewDTO> sheets = parseWorkbook(bytes);

            ImportValidationRequestDTO request = new ImportValidationRequestDTO();
            request.setSchoolId(normalizedSchoolId);
            request.setImportType(safeImportType);
            request.setFileName(fileName);
            request.setRequestedByRole(safeRole);
            request.setSheets(sheets);
            ImportPreviewResponseDTO preview = importValidationService.validatePreview(request);

            boolean duplicate = uploadRepository
                    .findFirstBySchoolCodeIgnoreCaseAndChecksumAndRolledBackFalseOrderByUploadedAtDesc(normalizedSchoolId, checksum)
                    .isPresent();
            if (duplicate) {
                preview.getIssues().add(new ImportValidationIssueDTO("Workbook", 0, "file", "WARNING", "This workbook was uploaded earlier for the same school_id. Review before committing again."));
                preview.setStatus(preview.isValid() ? "READY_WITH_WARNINGS" : preview.getStatus());
            }

            SchoolImportUpload upload = new SchoolImportUpload();
            upload.setSchoolCode(normalizedSchoolId);
            upload.setAcademicYear(safeYear);
            upload.setImportType(safeImportType);
            upload.setFileName(fileName);
            upload.setChecksum(checksum);
            upload.setStatus(preview.getStatus());
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
        upload.setStatus("COMMITTED_PENDING_DATA_SAVE");
        upload.setCommittedAt(LocalDateTime.now());
        uploadRepository.save(upload);
        return action(upload, "Import approved. Data-save hooks are ready for entity persistence in the next backend pass.");
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

    private List<ImportSheetPreviewDTO> parseWorkbook(byte[] bytes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(bytes))) {
            List<ImportSheetPreviewDTO> sheets = new ArrayList<>();
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
                int dataRows = 0;
                for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row != null && !isBlankRow(row)) dataRows++;
                }
                sheets.add(new ImportSheetPreviewDTO(sheet.getSheetName(), dataRows, headers));
            }
            return sheets;
        }
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
        dto.setCommitted(upload.isCommitted());
        dto.setRolledBack(upload.isRolledBack());
        dto.setMessage(message);
        dto.setActionAt(LocalDateTime.now());
        return dto;
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
        String resolved = TenantUtils.isValidSchoolId(requested) ? requested : context;
        if (!TenantUtils.isValidSchoolId(resolved)) {
            throw new IllegalArgumentException("school_id must be exactly 4 uppercase alphanumeric characters.");
        }
        if (TenantUtils.isValidSchoolId(requested) && context != null && !context.isBlank() && !requested.equals(context)) {
            throw new IllegalArgumentException("Request school_id does not match active tenant context.");
        }
        return resolved;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
