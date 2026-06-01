package com.school.attendance.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.dto.imports.ImportCommitResponseDTO;
import com.school.attendance.dto.imports.ImportUploadHistoryDTO;
import com.school.attendance.entity.SchoolImportUpload;
import com.school.attendance.repository.SchoolImportUploadRepository;
import com.school.attendance.repository.SchoolImportUploadSummaryProjection;
import com.school.attendance.service.WorkbookImportService;
import com.school.attendance.tenant.TenantUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping({"/workbook-commit", "/api/workbook-commit"})
public class WorkbookCommitController {

    private final WorkbookImportService workbookImportService;
    private final SchoolImportUploadRepository uploadRepository;

    public WorkbookCommitController(WorkbookImportService workbookImportService,
                                    SchoolImportUploadRepository uploadRepository) {
        this.workbookImportService = workbookImportService;
        this.uploadRepository = uploadRepository;
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status(@RequestParam String schoolId) {
        String tenantId = TenantUtils.requireValidSchoolId(schoolId);
        List<SchoolImportUploadSummaryProjection> uploads = uploadRepository.findUploadSummariesForSchool(tenantId);
        SchoolImportUploadSummaryProjection latest = uploads.isEmpty() ? null : uploads.get(0);
        SchoolImportUploadSummaryProjection latestCommitted = uploads.stream()
                .filter(upload -> upload.isCommitted() && !upload.isRolledBack())
                .max(Comparator.comparing(SchoolImportUploadSummaryProjection::getCommittedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        SchoolImportUploadSummaryProjection latestCommitCandidate = findLatestCommitCandidateSummary(uploads);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("schoolId", tenantId);
        data.put("hasWorkbook", latest != null);
        data.put("latestUploadId", latest == null ? null : latest.getId());
        data.put("latestFileName", latest == null ? null : latest.getFileName());
        data.put("latestStatus", latest == null ? "NO_WORKBOOK" : latest.getStatus());
        data.put("latestLifecycleMessage", latest == null ? "No school data workbook has been uploaded yet." : latest.getLifecycleMessage());
        data.put("readyToCommit", latestCommitCandidate != null);
        data.put("commitCandidateUploadId", latestCommitCandidate == null ? null : latestCommitCandidate.getId());
        data.put("committed", latestCommitted != null);
        data.put("committedWorkbookCount", uploads.stream().filter(upload -> upload.isCommitted() && !upload.isRolledBack()).count());
        data.put("latestCommittedUploadId", latestCommitted == null ? null : latestCommitted.getId());
        data.put("latestCommittedAt", latestCommitted == null ? null : latestCommitted.getCommittedAt());
        data.put("stagedRowCount", latestCommitted == null ? 0 : latestCommitted.getStagedRowCount());
        data.put("rolledBack", latest != null && latest.isRolledBack());
        data.put("auditCount", uploads.size());
        data.put("generatedAt", LocalDateTime.now());

        String message = latestCommitted != null
                ? "Workbook commit status loaded. A committed workbook is available for activation."
                : latestCommitCandidate != null
                  ? "Workbook commit status loaded. Workbook is ready to commit."
                  : "Workbook commit status loaded. Upload and validate a workbook before commit.";
        return ApiResponse.success(message, data);
    }

    @PostMapping("/execute")
    public ApiResponse<ImportCommitResponseDTO> execute(@RequestParam String schoolId,
                                                        @RequestParam(required = false) Long uploadId,
                                                        @RequestBody(required = false) Map<String, Object> request) {
        String tenantId = TenantUtils.requireValidSchoolId(resolveSchoolId(schoolId, request));
        Long targetUploadId = resolveUploadId(uploadId, request);
        if (targetUploadId == null) {
            targetUploadId = findLatestCommitCandidate(uploadRepository.findTop20BySchoolCodeIgnoreCaseOrderByUploadedAtDesc(tenantId)).getId();
        }
        return ApiResponse.success("Workbook commit execution completed", workbookImportService.commit(targetUploadId, tenantId));
    }

    @PostMapping("/rollback")
    public ApiResponse<ImportCommitResponseDTO> rollback(@RequestParam String schoolId,
                                                         @RequestParam(required = false) Long uploadId,
                                                         @RequestBody(required = false) Map<String, Object> request) {
        String tenantId = TenantUtils.requireValidSchoolId(resolveSchoolId(schoolId, request));
        Long targetUploadId = resolveUploadId(uploadId, request);
        if (targetUploadId == null) {
            targetUploadId = findLatestRollbackCandidate(uploadRepository.findTop20BySchoolCodeIgnoreCaseOrderByUploadedAtDesc(tenantId)).getId();
        }
        return ApiResponse.success("Workbook rollback completed", workbookImportService.rollback(targetUploadId, tenantId));
    }

    @GetMapping({"/audit-trail", "/commit-audit-trail"})
    public ApiResponse<List<ImportUploadHistoryDTO>> auditTrail(@RequestParam String schoolId) {
        return ApiResponse.success("Workbook commit audit trail loaded", workbookImportService.history(schoolId));
    }

    private SchoolImportUploadSummaryProjection findLatestCommitCandidateSummary(List<SchoolImportUploadSummaryProjection> uploads) {
        return uploads.stream()
                .filter(upload -> !upload.isCommitted())
                .filter(upload -> !upload.isRolledBack())
                .filter(upload -> upload.getErrorCount() == 0)
                .filter(upload -> !"BLOCKED".equalsIgnoreCase(safe(upload.getStatus())))
                .findFirst()
                .orElse(null);
    }

    private SchoolImportUpload findLatestCommitCandidate(List<SchoolImportUpload> uploads) {
        return uploads.stream()
                .filter(upload -> !upload.isCommitted())
                .filter(upload -> !upload.isRolledBack())
                .filter(upload -> upload.getErrorCount() == 0)
                .filter(upload -> !"BLOCKED".equalsIgnoreCase(safe(upload.getStatus())))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No validated workbook is available to commit. Upload and validate a clean workbook first."));
    }

    private SchoolImportUpload findLatestRollbackCandidate(List<SchoolImportUpload> uploads) {
        return uploads.stream()
                .filter(SchoolImportUpload::isCommitted)
                .filter(upload -> !upload.isRolledBack())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No committed workbook is available to roll back."));
    }

    private Long resolveUploadId(Long uploadId, Map<String, Object> request) {
        if (uploadId != null) return uploadId;
        if (request == null) return null;
        Object rawUploadId = request.get("uploadId");
        if (rawUploadId == null || String.valueOf(rawUploadId).isBlank()) return null;
        return Long.valueOf(String.valueOf(rawUploadId).trim());
    }

    private String resolveSchoolId(String schoolId, Map<String, Object> request) {
        if (schoolId != null && !schoolId.isBlank()) return schoolId;
        if (request == null) return schoolId;
        Object rawSchoolId = request.get("schoolId");
        return rawSchoolId == null ? schoolId : String.valueOf(rawSchoolId);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
