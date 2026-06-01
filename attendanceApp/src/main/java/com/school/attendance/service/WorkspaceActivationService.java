package com.school.attendance.service;

import com.school.attendance.dto.*;
import com.school.attendance.entity.SchoolImportUpload;
import com.school.attendance.repository.SchoolImportUploadRepository;
import com.school.attendance.tenant.TenantUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class WorkspaceActivationService {
    private final WorkspaceSetupService workspaceSetupService;
    private final SchoolImportUploadRepository importUploadRepository;

    public WorkspaceActivationService(WorkspaceSetupService workspaceSetupService,
                                      SchoolImportUploadRepository importUploadRepository) {
        this.workspaceSetupService = workspaceSetupService;
        this.importUploadRepository = importUploadRepository;
    }

    @Transactional(readOnly = true)
    public WorkspaceActivationSummaryDTO getSummary(String schoolId) {
        String tenantId = TenantUtils.requireValidSchoolId(schoolId);
        WorkspaceChecklistDTO checklist = workspaceSetupService.getOrCreate(tenantId);
        List<SchoolImportUpload> uploads = importUploadRepository.findTop20BySchoolCodeIgnoreCaseOrderByUploadedAtDesc(tenantId);
        List<SchoolImportUpload> committed = uploads.stream()
                .filter(upload -> upload.isCommitted() && !upload.isRolledBack())
                .sorted(Comparator.comparing(SchoolImportUpload::getCommittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        boolean schoolProfileReady = notBlank(checklist.getSchoolName());
        boolean academicYearReady = notBlank(checklist.getAcademicYear())
                && checklist.getAcademicYearStartDate() != null
                && checklist.getAcademicYearEndDate() != null;
        boolean workspaceSetupReady = !checklist.isImportLocked();
        boolean importCommitted = !committed.isEmpty();
        boolean readyForActivation = schoolProfileReady && academicYearReady && workspaceSetupReady && importCommitted;

        WorkspaceActivationSummaryDTO summary = new WorkspaceActivationSummaryDTO();
        summary.setSchoolId(tenantId);
        summary.setSchoolName(notBlank(checklist.getSchoolName()) ? checklist.getSchoolName() : tenantId + " School");
        summary.setAcademicYear(checklist.getAcademicYear());
        summary.setSchoolProfileReady(schoolProfileReady);
        summary.setAcademicYearReady(academicYearReady);
        summary.setWorkspaceSetupReady(workspaceSetupReady);
        summary.setImportCommitted(importCommitted);
        summary.setReadyForActivation(readyForActivation);
        summary.setCommittedWorkbookCount(committed.size());
        summary.setLastWorkbookCommittedAt(committed.stream().map(SchoolImportUpload::getCommittedAt).filter(v -> v != null).findFirst().orElse(null));
        summary.setWorkspaceChecklist(checklist);
        summary.setReadinessPercent(calculateReadiness(schoolProfileReady, academicYearReady, workspaceSetupReady, importCommitted));
        summary.setActivationStatus(readyForActivation ? "READY_FOR_ACTIVATION" : "PENDING_CONFIGURATION");
        summary.setActivationMessage(readyForActivation
                ? "School workspace is ready for activation. Admin and Principal can use the health center before enabling live operations."
                : "Complete School Profile, Academic Year, Workspace Setup, and committed workbook import before activation.");
        summary.setHealthItems(List.of(
                health("SCHOOL_PROFILE", "School Profile", schoolProfileReady, schoolProfileReady ? "School identity is configured." : "Add school name and profile details."),
                health("ACADEMIC_YEAR", "Academic Year", academicYearReady, academicYearReady ? "Academic year dates are configured." : "Activate the academic year with start and end dates."),
                health("WORKSPACE_SETUP", "Workspace Setup", workspaceSetupReady, workspaceSetupReady ? "Workspace checklist is complete." : checklist.getImportLockMessage()),
                health("WORKBOOK_IMPORT", "School Data Workbook", importCommitted, importCommitted ? "Validated workbook has been committed." : "Upload, validate, and commit the workbook from Web ERP."),
                health("TENANT_ISOLATION", "Tenant Isolation", true, "Requests are bound to school_id " + tenantId + ".")
        ));
        summary.setAuditTrail(buildAuditTrail(checklist, uploads, readyForActivation));
        return summary;
    }

    @Transactional(readOnly = true)
    public WorkspaceActivationSummaryDTO activate(String schoolId, SchoolActivationRequestDTO request) {
        WorkspaceActivationSummaryDTO summary = getSummary(schoolId);
        if (!summary.isReadyForActivation()) {
            throw new IllegalStateException("Workspace is not ready for activation. Complete all readiness checks first.");
        }
        summary.setActivationStatus("ACTIVE_READY");
        summary.setActivationMessage("Workspace activation checks passed. School operations can proceed with Admin/Principal monitoring.");
        summary.getAuditTrail().add(0, new WorkspaceActivationAuditDTO(
                "ACTIVATION_CHECK",
                "Activation checks passed",
                clean(request == null ? null : request.getRemarks(), "Workspace activation verified by Admin/Principal."),
                "ACTIVE_READY",
                LocalDateTime.now()
        ));
        return summary;
    }

    private List<WorkspaceActivationAuditDTO> buildAuditTrail(WorkspaceChecklistDTO checklist, List<SchoolImportUpload> uploads, boolean ready) {
        List<WorkspaceActivationAuditDTO> audit = new java.util.ArrayList<>();
        audit.add(new WorkspaceActivationAuditDTO("WORKSPACE_SETUP", "Workspace setup reviewed",
                checklist.getCompletedSteps() + " of " + checklist.getTotalSteps() + " setup steps completed.",
                checklist.isImportLocked() ? "PENDING" : "COMPLETE", checklist.getUpdatedAt()));
        uploads.stream().limit(5).forEach(upload -> audit.add(new WorkspaceActivationAuditDTO("WORKBOOK_IMPORT",
                upload.isCommitted() ? "Workbook committed" : "Workbook uploaded",
                upload.getFileName() + " • " + upload.getTotalSheets() + " sheets • " + upload.getTotalRows() + " rows",
                upload.isRolledBack() ? "ROLLED_BACK" : upload.isCommitted() ? "COMMITTED" : clean(upload.getStatus(), "UPLOADED"),
                upload.isCommitted() && upload.getCommittedAt() != null ? upload.getCommittedAt() : upload.getUploadedAt())));
        audit.add(new WorkspaceActivationAuditDTO("TENANT_STATUS", "Activation readiness calculated",
                ready ? "All activation gates are passing." : "One or more activation gates are pending.",
                ready ? "READY" : "PENDING", LocalDateTime.now()));
        return audit;
    }

    private WorkspaceHealthItemDTO health(String key, String label, boolean ready, String message) {
        return new WorkspaceHealthItemDTO(key, label, ready ? "READY" : "PENDING", message);
    }

    private int calculateReadiness(boolean... checks) {
        int pass = 0;
        for (boolean check : checks) if (check) pass++;
        return Math.round((pass * 100f) / checks.length);
    }

    private boolean notBlank(String value) { return value != null && !value.isBlank(); }
    private String clean(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim(); }
}
