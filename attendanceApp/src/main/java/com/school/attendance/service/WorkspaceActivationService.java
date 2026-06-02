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

    @Transactional(readOnly = true)
    public ActivationOperationsCenterDTO operationsCenter(String schoolId) {
        WorkspaceActivationSummaryDTO summary = buildSummary(schoolId, null);
        ActivationOperationsCenterDTO center = new ActivationOperationsCenterDTO();
        center.setSchoolId(summary.getSchoolId());
        center.setSchoolName(summary.getSchoolName());
        center.setActivationStatus(summary.getActivationStatus());
        center.setReadinessPercent(summary.getReadinessPercent());
        center.setReadyForActivation(summary.isReadyForActivation());
        center.setTenantActive(summary.isTenantActive());
        center.setReportingStatus(summary.isTenantActive() ? "LIVE_REPORTING_ENABLED" : summary.isReadyForActivation() ? "READY_FOR_ADMIN_PRINCIPAL_ACTIVATION" : "REPORTING_PRE_ACTIVATION");
        center.setOperationsNote(summary.isTenantActive()
                ? "Activation is complete. Admin and Principal can use this center for activation reporting and audit review."
                : summary.isReadyForActivation()
                  ? "All gates are ready. Admin or Principal can activate the workspace after final review."
                  : "Activation remains blocked until all readiness gates are complete.");
        center.setTimeline(summary.getAuditTrail().stream()
                .map(item -> new ActivationOperationStepDTO(item.getEventType(), item.getTitle(), item.getStatus(), item.getDescription(), item.getEventAt()))
                .toList());
        center.setAdminPrincipalReportCards(summary.getHealthItems());
        center.setNotesHistory(summary.getAuditTrail().stream()
                .map(item -> item.getTitle() + " — " + item.getDescription())
                .limit(10)
                .toList());
        return center;
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

        String previousStatus = onboarding == null ? "READY_FOR_ACTIVATION" : clean(onboarding.getStatus(), "PENDING").toUpperCase(Locale.ROOT);
        if (onboarding != null) {
            transitionOnboardingToActive(onboarding, actor, remarks, now);
        }

        WorkspaceActivationSummaryDTO summary = buildSummary(tenantId, new WorkspaceActivationAuditDTO(
                "TENANT_STATUS",
                "Tenant status transitioned " + previousStatus + " → ACTIVE",
                remarks + " Activation engine completed the approved lifecycle path: PENDING → APPROVED → PILOT → ACTIVE.",
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
        String registrationLifecycleStatus = onboarding.map(item -> clean(item.getStatus(), "PENDING").toUpperCase(Locale.ROOT)).orElse("PENDING");
        boolean tenantActive = readyForActivation && ("ACTIVE".equalsIgnoreCase(registrationLifecycleStatus) || isSystemTenant(tenantId));
        String lifecycleStatus = resolveTenantLifecycleStatus(importCommitted, tenantActive, registrationLifecycleStatus);

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
        summary.setGoLiveStatus(tenantActive ? "LIVE_READY" : readyForActivation ? "READY_FOR_GO_LIVE" : "NOT_READY");
        summary.setActivationButtonLabel(buttonLabel(status));
        summary.setActivationMessage(message(status));
        summary.setTenantLifecycleStatus(lifecycleStatus);
        summary.setActivationStage(resolveActivationStage(status, lifecycleStatus));
        summary.setCredentialProvisioningStatus(resolveCredentialStatus(onboarding.orElse(null), tenantActive));
        summary.setActivationSuccessTitle(tenantActive ? "School Workspace Activated" : readyForActivation ? "Ready for School Activation" : "Activation Readiness Pending");
        summary.setActivationSuccessMessage(tenantActive
                ? "Tenant status is ACTIVE. Admin and Principal login access is enabled for live ERP operations."
                : readyForActivation
                  ? "All readiness gates passed. Activate Workspace will complete the PENDING → APPROVED → PILOT → ACTIVE lifecycle."
                  : message(status));
        summary.setActivationNotifications(buildNotifications(tenantId, status, lifecycleStatus, tenantActive, readyForActivation, committed.size()));
        summary.setHealthItems(List.of(
                health("SCHOOL_PROFILE", "School Profile", schoolProfileReady, schoolProfileReady ? "School identity is configured." : "Add school name and profile details."),
                health("ACADEMIC_YEAR", "Academic Year", academicYearReady, academicYearReady ? "Academic year dates are configured." : "Activate the academic year with start and end dates."),
                health("WORKSPACE_SETUP", "Workspace Setup", workspaceSetupReady, workspaceSetupReady ? "Workspace checklist is complete." : checklist.getImportLockMessage()),
                health("WORKBOOK_IMPORT", "School Data Workbook", importCommitted, importCommitted ? "Validated workbook has been committed." : "Upload, validate, and commit the workbook from Web ERP."),
                health("GO_LIVE_STATUS", "Go-Live Status", tenantActive, tenantActive ? "School workspace is active for live ERP operations." : "Go-live is blocked until workbook commit and activation are completed."),
                health("TENANT_ISOLATION", "Tenant Isolation", true, "Requests are securely bound to School ID: " + tenantId)
        ));
        summary.setAuditTrail(buildAuditTrail(checklist, uploads, readyForActivation, tenantActive, onboarding.orElse(null), activationEvent));
        return summary;
    }

    private void transitionOnboardingToActive(SchoolOnboardingRequest onboarding, String actor, String remarks, LocalDateTime now) {
        String currentStatus = clean(onboarding.getStatus(), "PENDING").toUpperCase(Locale.ROOT);
        if ("REJECTED".equals(currentStatus)) {
            throw new IllegalStateException("Rejected school registrations cannot be activated. Reopen or create a new registration request first.");
        }
        if (onboarding.getApprovedAt() == null) {
            onboarding.setApprovedAt(now);
            onboarding.setApprovedBy(actor);
        }
        if (onboarding.getPilotActivatedAt() == null) {
            onboarding.setPilotActivatedAt(now);
            onboarding.setPilotEnabledBy(actor);
        }
        onboarding.setStatus("ACTIVE");
        onboarding.setActivatedAt(now);
        onboarding.setActivatedBy(actor);
        onboarding.setReviewNotes(remarks);
        onboarding.setUpdatedAt(now);
        onboardingRepository.save(onboarding);
    }

    private boolean isSystemTenant(String tenantId) {
        return "BRK1".equalsIgnoreCase(tenantId) || "DEMO".equalsIgnoreCase(tenantId);
    }

    private String resolveTenantLifecycleStatus(boolean importCommitted, boolean tenantActive, String registrationLifecycleStatus) {
        if (tenantActive) return "ACTIVE";
        if (importCommitted) return "PILOT";
        if ("APPROVED".equalsIgnoreCase(registrationLifecycleStatus)) return "APPROVED";
        return "PENDING";
    }

    private String resolveActivationStage(String status, String lifecycleStatus) {
        if ("ACTIVE".equals(status)) return "ACTIVE_GO_LIVE";
        if ("READY_FOR_ACTIVATION".equals(status)) return "READY_TO_ACTIVATE";
        if ("PILOT".equalsIgnoreCase(lifecycleStatus)) return "PILOT_VALIDATION";
        if ("APPROVED".equalsIgnoreCase(lifecycleStatus)) return "APPROVED_WORKSPACE_SETUP";
        if ("PENDING_WORKBOOK_COMMIT".equals(status)) return "WORKBOOK_COMMIT_PENDING";
        if ("PENDING_WORKSPACE_SETUP".equals(status)) return "WORKSPACE_SETUP_PENDING";
        return "CONFIGURATION_PENDING";
    }

    private String resolveCredentialStatus(SchoolOnboardingRequest onboarding, boolean tenantActive) {
        if (!tenantActive) return "LOCKED_UNTIL_ACTIVE";
        if (onboarding == null) return "SYSTEM_TENANT";
        boolean adminReady = notBlank(onboarding.getAdminUsername());
        boolean principalReady = notBlank(onboarding.getPrincipalUsername());
        return adminReady && principalReady ? "ISSUED" : "READY_TO_ISSUE";
    }

    private List<String> buildNotifications(String tenantId, String status, String lifecycleStatus, boolean tenantActive, boolean readyForActivation, int committedWorkbookCount) {
        List<String> notifications = new java.util.ArrayList<>();
        notifications.add("Requests are securely bound to School ID: " + tenantId);
        notifications.add("Tenant lifecycle status: " + lifecycleStatus);
        if (tenantActive) {
            notifications.add("Activation completed. School workspace is live ready for Admin and Principal operations.");
            notifications.add("Credentials can be issued or regenerated from the activation package workflow when required.");
        } else if (readyForActivation) {
            notifications.add("All readiness gates passed. Activate Workspace is now available.");
        } else if ("PENDING_WORKBOOK_COMMIT".equals(status)) {
            notifications.add("Workbook commit is required before activation. Committed workbook count: " + committedWorkbookCount);
        } else {
            notifications.add("Complete Workspace Setup and committed workbook import before activation.");
        }
        return notifications;
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
