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
import java.util.Locale;
import java.util.Optional;

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
        return buildSummary(schoolId, null);
    }

    @Transactional
    public WorkspaceActivationSummaryDTO activate(String schoolId, SchoolActivationRequestDTO request) {
        String tenantId = TenantUtils.requireValidSchoolId(schoolId);
        WorkspaceActivationSummaryDTO preCheck = buildSummary(tenantId, null);
        if (!preCheck.isReadyForActivation()) {
            throw new IllegalStateException("Commit a valid school data workbook before activating the workspace.");
        }

        SchoolOnboardingRequest onboarding = onboardingRepository.findTopBySchoolIdOrderByUpdatedAtDesc(tenantId)
                .orElse(null);
        LocalDateTime now = LocalDateTime.now();
        String actor = clean(request == null ? null : request.getActivatedBy(), "VidyaSetu Onboarding Team");
        String remarks = clean(request == null ? null : request.getRemarks(), "Workspace activated after all readiness gates passed.");

        if (onboarding != null) {
            onboarding.setStatus("ACTIVE");
            onboarding.setActivatedAt(now);
            onboarding.setActivatedBy(actor);
            onboarding.setUpdatedAt(now);
            onboardingRepository.save(onboarding);
        }

        WorkspaceActivationSummaryDTO summary = buildSummary(tenantId, new WorkspaceActivationAuditDTO(
                "TENANT_STATUS",
                "Tenant status transitioned to ACTIVE",
                remarks,
                "ACTIVE",
                now
        ));
        summary.setActivatedBy(actor);
        summary.setActivatedAt(now);
        return summary;
    }

    private WorkspaceActivationSummaryDTO buildSummary(String schoolId, WorkspaceActivationAuditDTO activationEvent) {
        String tenantId = TenantUtils.requireValidSchoolId(schoolId);
        WorkspaceChecklistDTO checklist = workspaceSetupService.getOrCreate(tenantId);
        List<SchoolImportUpload> uploads = importUploadRepository.findTop20BySchoolCodeIgnoreCaseOrderByUploadedAtDesc(tenantId);
        List<SchoolImportUpload> committed = uploads.stream()
                .filter(upload -> upload.isCommitted() && !upload.isRolledBack())
                .sorted(Comparator.comparing(SchoolImportUpload::getCommittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        Optional<SchoolOnboardingRequest> onboarding = onboardingRepository.findTopBySchoolIdOrderByUpdatedAtDesc(tenantId);

        boolean schoolProfileReady = notBlank(checklist.getSchoolName());
        boolean academicYearReady = notBlank(checklist.getAcademicYear())
                && checklist.getAcademicYearStartDate() != null
                && checklist.getAcademicYearEndDate() != null;
        boolean workspaceSetupReady = !checklist.isImportLocked();
        boolean importCommitted = !committed.isEmpty();
        boolean readyForActivation = schoolProfileReady && academicYearReady && workspaceSetupReady && importCommitted;
        boolean tenantActive = readyForActivation && onboarding.map(item -> "ACTIVE".equalsIgnoreCase(item.getStatus())).orElse(false);

        WorkspaceActivationSummaryDTO summary = new WorkspaceActivationSummaryDTO();
        summary.setSchoolId(tenantId);
        summary.setSchoolName(notBlank(checklist.getSchoolName()) ? checklist.getSchoolName() : tenantId + " School");
        summary.setAcademicYear(checklist.getAcademicYear());
        summary.setSchoolProfileReady(schoolProfileReady);
        summary.setAcademicYearReady(academicYearReady);
        summary.setWorkspaceSetupReady(workspaceSetupReady);
        summary.setImportCommitted(importCommitted);
        summary.setTenantActive(tenantActive);
        summary.setReadyForActivation(readyForActivation && !tenantActive);
        summary.setCommittedWorkbookCount(committed.size());
        summary.setLastWorkbookCommittedAt(committed.stream().map(SchoolImportUpload::getCommittedAt).filter(v -> v != null).findFirst().orElse(null));
        summary.setWorkspaceChecklist(checklist);
        summary.setReadinessPercent(calculateReadiness(schoolProfileReady, academicYearReady, workspaceSetupReady, importCommitted, tenantActive));
        if (tenantActive) {
            onboarding.ifPresent(item -> {
                summary.setActivatedBy(item.getActivatedBy());
                summary.setActivatedAt(item.getActivatedAt());
            });
        } else {
            summary.setActivatedBy(null);
            summary.setActivatedAt(null);
        }

        String status = resolveStatus(readyForActivation, tenantActive, importCommitted, workspaceSetupReady);
        summary.setActivationStatus(status);
        summary.setGoLiveStatus(tenantActive ? "LIVE_READY" : "NOT_READY");
        summary.setActivationButtonLabel(buttonLabel(status));
        summary.setActivationMessage(message(status));
        summary.setHealthItems(List.of(
                health("SCHOOL_PROFILE", "School Profile", schoolProfileReady, schoolProfileReady ? "School identity is configured." : "Add school name and profile details."),
                health("ACADEMIC_YEAR", "Academic Year", academicYearReady, academicYearReady ? "Academic year dates are configured." : "Activate the academic year with start and end dates."),
                health("WORKSPACE_SETUP", "Workspace Setup", workspaceSetupReady, workspaceSetupReady ? "Workspace checklist is complete." : checklist.getImportLockMessage()),
                health("WORKBOOK_IMPORT", "School Data Workbook", importCommitted, importCommitted ? "Validated workbook has been committed." : "Upload, validate, and commit the workbook from Web ERP."),
                health("GO_LIVE_STATUS", "Go-Live Status", tenantActive, tenantActive ? "School workspace is active for live ERP operations." : "Go-live is blocked until workbook commit and activation are completed."),
                health("TENANT_ISOLATION", "Tenant Isolation", true, "Requests are bound to school_id " + tenantId + ".")
        ));
        summary.setAuditTrail(buildAuditTrail(checklist, uploads, readyForActivation, tenantActive, onboarding.orElse(null), activationEvent));
        return summary;
    }

    private String resolveStatus(boolean readyForActivation, boolean tenantActive, boolean importCommitted, boolean workspaceSetupReady) {
        if (tenantActive) return "ACTIVE";
        if (readyForActivation) return "READY_FOR_ACTIVATION";
        if (!workspaceSetupReady) return "PENDING_WORKSPACE_SETUP";
        if (!importCommitted) return "PENDING_WORKBOOK_COMMIT";
        return "PENDING_CONFIGURATION";
    }

    private String buttonLabel(String status) {
        return switch (status) {
            case "ACTIVE" -> "Activation Completed";
            case "READY_FOR_ACTIVATION" -> "Activate Workspace";
            case "PENDING_WORKBOOK_COMMIT" -> "Commit Workbook First";
            default -> "Activation Pending";
        };
    }

    private String message(String status) {
        return switch (status) {
            case "ACTIVE" -> "School workspace is active. Tenant status is ACTIVE and ERP login access is enabled.";
            case "READY_FOR_ACTIVATION" -> "All readiness gates are complete. Activate the school workspace to enable live operations.";
            case "PENDING_WORKBOOK_COMMIT" -> "Workbook commit is pending. Upload, validate, and commit a clean school data workbook before activation.";
            case "PENDING_WORKSPACE_SETUP" -> "Complete Workspace Setup before importing and committing the school data workbook.";
            default -> "Complete School Profile, Academic Year, Workspace Setup, and committed workbook import before activation.";
        };
    }

    private List<WorkspaceActivationAuditDTO> buildAuditTrail(WorkspaceChecklistDTO checklist,
                                                              List<SchoolImportUpload> uploads,
                                                              boolean ready,
                                                              boolean active,
                                                              SchoolOnboardingRequest onboarding,
                                                              WorkspaceActivationAuditDTO activationEvent) {
        List<WorkspaceActivationAuditDTO> audit = new java.util.ArrayList<>();
        if (activationEvent != null) audit.add(activationEvent);
        audit.add(new WorkspaceActivationAuditDTO("WORKSPACE_SETUP", "Workspace setup reviewed",
                checklist.getCompletedSteps() + " of " + checklist.getTotalSteps() + " setup steps completed.",
                checklist.isImportLocked() ? "PENDING" : "COMPLETE", checklist.getUpdatedAt()));
        uploads.stream().limit(8).forEach(upload -> audit.add(new WorkspaceActivationAuditDTO("WORKBOOK_IMPORT",
                upload.isCommitted() ? "Workbook committed" : "Workbook uploaded",
                upload.getFileName() + " • " + upload.getTotalSheets() + " sheets • " + upload.getTotalRows() + " rows",
                upload.isRolledBack() ? "ROLLED_BACK" : upload.isCommitted() ? "COMMITTED" : clean(upload.getStatus(), "UPLOADED"),
                upload.isCommitted() && upload.getCommittedAt() != null ? upload.getCommittedAt() : upload.getUploadedAt())));
        if (onboarding != null && active) {
            audit.add(new WorkspaceActivationAuditDTO("TENANT_STATUS", "Tenant status transitioned to ACTIVE",
                    "School workspace activation is complete and login access is enabled.",
                    "ACTIVE", onboarding.getActivatedAt()));
        }
        audit.add(new WorkspaceActivationAuditDTO("ACTIVATION_READINESS", "Activation readiness calculated",
                active ? "All activation gates are complete and school is live ready." : ready ? "All activation gates are passing. Activate Workspace is available." : "One or more activation gates are pending.",
                active ? "ACTIVE" : ready ? "READY" : "PENDING", LocalDateTime.now()));
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
