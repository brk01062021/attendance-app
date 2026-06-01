package com.school.attendance.service;

import com.school.attendance.dto.*;
import com.school.attendance.entity.SchoolImportUpload;
import com.school.attendance.entity.SchoolOnboardingRequest;
import com.school.attendance.repository.SchoolImportUploadRepository;
import com.school.attendance.repository.SchoolOnboardingRequestRepository;
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
    private final SchoolOnboardingRequestRepository onboardingRepository;

    public WorkspaceActivationService(WorkspaceSetupService workspaceSetupService,
                                      SchoolImportUploadRepository importUploadRepository,
                                      SchoolOnboardingRequestRepository onboardingRepository) {
        this.workspaceSetupService = workspaceSetupService;
        this.importUploadRepository = importUploadRepository;
        this.onboardingRepository = onboardingRepository;
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
        SchoolOnboardingRequest onboarding = onboardingRepository.findTopBySchoolIdOrderByUpdatedAtDesc(tenantId).orElse(null);
        boolean active = onboarding != null && "ACTIVE".equalsIgnoreCase(onboarding.getStatus());

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
        summary.setActivatedAt(onboarding == null ? null : onboarding.getActivatedAt());
        summary.setActivatedBy(onboarding == null ? null : onboarding.getActivatedBy());
        summary.setActivationStatus(active ? "ACTIVE" : readyForActivation ? "READY_FOR_ACTIVATION" : "PENDING_CONFIGURATION");
        summary.setGoLiveStatus(active ? "LIVE_READY" : readyForActivation ? "READY_TO_GO_LIVE" : "SETUP_PENDING");
        summary.setNextStep(active ? "School workspace is active. Continue daily ERP operations with Admin/Principal monitoring." : readyForActivation ? "Review the Go-Live readiness checks and activate the school workspace." : "Complete Workspace Setup, commit the workbook, then return to School Activation.");
        summary.setActivationMessage(active
                ? "School workspace is active. Tenant status is ACTIVE and ERP login access is enabled."
                : readyForActivation
                  ? "School workspace is ready for activation. Admin and Principal can use the health center before enabling live operations."
                  : "Complete School Profile, Academic Year, Workspace Setup, and committed workbook import before activation.");
        summary.setHealthItems(List.of(
                health("SCHOOL_PROFILE", "School Profile", schoolProfileReady, schoolProfileReady ? "School identity is configured." : "Add school name and profile details."),
                health("ACADEMIC_YEAR", "Academic Year", academicYearReady, academicYearReady ? "Academic year dates are configured." : "Activate the academic year with start and end dates."),
                health("WORKSPACE_SETUP", "Workspace Setup", workspaceSetupReady, workspaceSetupReady ? "Workspace checklist is complete." : checklist.getImportLockMessage()),
                health("WORKBOOK_IMPORT", "School Data Workbook", importCommitted, importCommitted ? "Validated workbook has been committed." : "Upload, validate, and commit the workbook from Web ERP."),
                health("TENANT_STATUS", "Tenant Status", active, active ? "Tenant status is ACTIVE for live ERP operations." : "Tenant is not active yet. Activation will move the school to ACTIVE."),
                health("TENANT_ISOLATION", "Tenant Isolation", true, "Requests are bound to school_id " + tenantId + ".")
        ));
        summary.setAuditTrail(buildAuditTrail(checklist, uploads, readyForActivation, onboarding));
        return summary;
    }

    @Transactional
    public WorkspaceActivationSummaryDTO activate(String schoolId, SchoolActivationRequestDTO request) {
        WorkspaceActivationSummaryDTO summary = getSummary(schoolId);
        if (!summary.isReadyForActivation()) {
            throw new IllegalStateException("Workspace is not ready for activation. Complete all readiness checks first.");
        }
        String activatedBy = clean(request == null ? null : request.getActivatedBy(), "VidyaSetu Onboarding Team");
        SchoolOnboardingRequest onboarding = onboardingRepository.findTopBySchoolIdOrderByUpdatedAtDesc(summary.getSchoolId()).orElse(null);
        if (onboarding != null) {
            onboarding.setStatus("ACTIVE");
            onboarding.setActivatedBy(activatedBy);
            if (onboarding.getActivatedAt() == null) onboarding.setActivatedAt(LocalDateTime.now());
            onboarding.setUpdatedAt(LocalDateTime.now());
            onboarding.setReviewNotes(clean(request == null ? null : request.getRemarks(), "School workspace activated after workbook commit and readiness validation."));
            onboardingRepository.save(onboarding);
        }

        summary = getSummary(summary.getSchoolId());
        summary.setActivationStatus("ACTIVE");
        summary.setGoLiveStatus("LIVE_READY");
        summary.setNextStep("School workspace is active. Continue with Admin/Principal Activation Summary and daily ERP monitoring.");
        summary.setActivationMessage("Activation completed successfully. Tenant status is ACTIVE and the school is ready for live ERP operations.");
        summary.setActivatedBy(activatedBy);
        summary.setActivatedAt(onboarding == null ? LocalDateTime.now() : onboarding.getActivatedAt());
        summary.getAuditTrail().add(0, new WorkspaceActivationAuditDTO(
                "SCHOOL_ACTIVATION",
                "School workspace activated",
                clean(request == null ? null : request.getRemarks(), "Workbook commit, workspace setup, tenant isolation, and readiness gates passed."),
                "ACTIVE",
                LocalDateTime.now()
        ));
        return summary;
    }

    private List<WorkspaceActivationAuditDTO> buildAuditTrail(WorkspaceChecklistDTO checklist, List<SchoolImportUpload> uploads, boolean ready, SchoolOnboardingRequest onboarding) {
        List<WorkspaceActivationAuditDTO> audit = new java.util.ArrayList<>();
        audit.add(new WorkspaceActivationAuditDTO("WORKSPACE_SETUP", "Workspace setup reviewed",
                checklist.getCompletedSteps() + " of " + checklist.getTotalSteps() + " setup steps completed.",
                checklist.isImportLocked() ? "PENDING" : "COMPLETE", checklist.getUpdatedAt()));
        uploads.stream().limit(5).forEach(upload -> audit.add(new WorkspaceActivationAuditDTO("WORKBOOK_IMPORT",
                upload.isCommitted() ? "Workbook committed" : "Workbook uploaded",
                upload.getFileName() + " • " + upload.getTotalSheets() + " sheets • " + upload.getTotalRows() + " rows",
                upload.isRolledBack() ? "ROLLED_BACK" : upload.isCommitted() ? "COMMITTED" : clean(upload.getStatus(), "UPLOADED"),
                upload.isCommitted() && upload.getCommittedAt() != null ? upload.getCommittedAt() : upload.getUploadedAt())));
        if (onboarding != null && "ACTIVE".equalsIgnoreCase(onboarding.getStatus())) {
            audit.add(new WorkspaceActivationAuditDTO("TENANT_STATUS", "Tenant status transitioned to ACTIVE",
                    "School workspace activation is complete and login access is enabled.",
                    "ACTIVE", onboarding.getActivatedAt() == null ? onboarding.getUpdatedAt() : onboarding.getActivatedAt()));
        }
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
