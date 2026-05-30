package com.school.attendance.service.onboarding;

import com.school.attendance.dto.onboarding.ActivationCredentialDTO;
import com.school.attendance.dto.onboarding.ActivationPackageDTO;
import com.school.attendance.dto.onboarding.OnboardingReviewItemDTO;
import com.school.attendance.dto.onboarding.OnboardingStatusResponseDTO;
import com.school.attendance.dto.onboarding.OnboardingStatusUpdateRequestDTO;
import com.school.attendance.dto.onboarding.PilotDemoRequestDTO;
import com.school.attendance.dto.onboarding.SchoolIdAvailabilityResponseDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationRequestDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationResponseDTO;
import com.school.attendance.dto.onboarding.WorkspaceProvisioningStepDTO;
import com.school.attendance.entity.AppUser;
import com.school.attendance.entity.SchoolOnboardingRequest;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.repository.SchoolOnboardingRequestRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.security.SecureRandom;

@Service
public class SchoolRegistrationService {
    private static final String SCHOOL_ID_PATTERN = "^[A-Z0-9]{4}$";
    private static final List<String> BLOCKING_SCHOOL_ID_STATUSES = List.of("RESERVED", "PENDING", "APPROVED", "PILOT", "ACTIVE");
    private static final List<String> REVIEW_QUEUE_STATUSES = List.of("PENDING", "APPROVED", "PILOT", "ACTIVE", "RESERVED", "REJECTED");
    private static final String VIDYASETU_ONBOARDING_TEAM = "VidyaSetu Onboarding Team";
    private static final String SCHOOL_REGISTRATION_PORTAL = "School Registration Portal";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final SchoolOnboardingRequestRepository onboardingRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public SchoolRegistrationService(SchoolOnboardingRequestRepository onboardingRepository, AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.onboardingRepository = onboardingRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SchoolIdAvailabilityResponseDTO checkSchoolId(String rawSchoolId) {
        String schoolId = normalizeSchoolId(rawSchoolId);
        if (!schoolId.matches(SCHOOL_ID_PATTERN)) {
            return new SchoolIdAvailabilityResponseDTO(schoolId, false, "INVALID", "School ID must be exactly 4 uppercase letters/numbers.");
        }
        if (isSystemReservedSchoolId(schoolId) || onboardingRepository.existsBySchoolIdAndStatusIn(schoolId, BLOCKING_SCHOOL_ID_STATUSES)) {
            return new SchoolIdAvailabilityResponseDTO(schoolId, false, "RESERVED", "School ID is already reserved, pending review, pilot, or active.");
        }
        return new SchoolIdAvailabilityResponseDTO(schoolId, true, "AVAILABLE", "School ID is available for registration.");
    }

    @Transactional
    public SchoolIdAvailabilityResponseDTO reserveSchoolId(String rawSchoolId) {
        SchoolIdAvailabilityResponseDTO availability = checkSchoolId(rawSchoolId);
        if (!availability.isAvailable()) return availability;

        LocalDateTime now = LocalDateTime.now();
        SchoolOnboardingRequest request = new SchoolOnboardingRequest();
        request.setReferenceId(buildReferenceId("RSV"));
        request.setSchoolId(availability.getSchoolId());
        request.setSchoolName("Reserved School Workspace");
        request.setRequestType("SCHOOL_ID_RESERVATION");
        request.setStatus("RESERVED");
        request.setReservedAt(now);
        request.setUpdatedAt(now);
        appendAudit(request, "SYSTEM", "NEW", "RESERVED", "school_id reserved from login onboarding flow", now);
        onboardingRepository.save(request);

        return new SchoolIdAvailabilityResponseDTO(availability.getSchoolId(), false, "RESERVED", "School ID reserved for onboarding. Continue registration to complete the request.");
    }

    @Transactional
    public SchoolRegistrationResponseDTO registerSchool(SchoolRegistrationRequestDTO request) {
        String schoolId = normalizeSchoolId(request.getRequestedSchoolId());
        if (!schoolId.matches(SCHOOL_ID_PATTERN)) {
            throw new IllegalArgumentException("School ID must be exactly 4 uppercase letters/numbers.");
        }
        if (isSystemReservedSchoolId(schoolId)) {
            throw new IllegalArgumentException("School ID is reserved for system/demo use.");
        }

        LocalDateTime now = LocalDateTime.now();
        SchoolOnboardingRequest onboarding = onboardingRepository.findTopBySchoolIdOrderByUpdatedAtDesc(schoolId).orElse(null);
        String fromStatus = onboarding == null ? "NEW" : onboarding.getStatus();
        if (onboarding != null && !"RESERVED".equals(onboarding.getStatus()) && !"REJECTED".equals(onboarding.getStatus())) {
            throw new IllegalArgumentException("School ID is already in onboarding lifecycle: " + onboarding.getStatus());
        }
        if (onboarding == null || "REJECTED".equals(onboarding.getStatus())) {
            onboarding = new SchoolOnboardingRequest();
            onboarding.setReferenceId(buildReferenceId("REG"));
            onboarding.setSchoolId(schoolId);
            onboarding.setReservedAt(now);
        }
        onboarding.setSchoolName(safeText(request.getSchoolName()));
        onboarding.setRequestType("SCHOOL_REGISTRATION");
        onboarding.setStatus("PENDING");
        onboarding.setContactPerson(safeText(request.getContactPerson()));
        onboarding.setContactPhone(safeText(request.getContactPhone()));
        onboarding.setContactEmail(safeText(request.getContactEmail()));
        onboarding.setCity(safeText(request.getCity()));
        onboarding.setState(safeText(request.getState()));
        onboarding.setExpectedStudents(request.getExpectedStudents());
        onboarding.setExpectedTeachers(request.getExpectedTeachers());
        onboarding.setNotes(safeText(request.getNotes()));
        onboarding.setSubmittedAt(now);
        onboarding.setSubmittedBy(SCHOOL_REGISTRATION_PORTAL);
        onboarding.setUpdatedAt(now);
        appendAudit(onboarding, SCHOOL_REGISTRATION_PORTAL, fromStatus, "PENDING", "Registration submitted for VidyaSetu Onboarding Team review", now);
        onboardingRepository.save(onboarding);

        return toRegistrationResponse(onboarding, "Registration Submitted successfully.", "VidyaSetu Onboarding Team will review the request and move it through Approved, Pilot, Active, and Credentials Issued stages. Final Excel import remains disabled.");
    }

    @Transactional
    public SchoolRegistrationResponseDTO requestPilotDemo(PilotDemoRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();
        SchoolOnboardingRequest onboarding = new SchoolOnboardingRequest();
        onboarding.setReferenceId(buildReferenceId("PILOT"));
        onboarding.setSchoolName(safeText(request.getSchoolName()));
        onboarding.setRequestType("PILOT_DEMO_REQUEST");
        onboarding.setStatus("PENDING");
        onboarding.setContactPerson(safeText(request.getContactPerson()));
        onboarding.setContactPhone(safeText(request.getContactPhone()));
        onboarding.setContactEmail(safeText(request.getContactEmail()));
        onboarding.setPreferredRole(safeText(request.getPreferredRole()));
        onboarding.setCity(safeText(request.getCity()));
        onboarding.setState(safeText(request.getState()));
        onboarding.setExpectedStudents(request.getExpectedStudents());
        onboarding.setExpectedTeachers(null);
        onboarding.setNotes(safeText(request.getNotes()));
        onboarding.setSubmittedAt(now);
        onboarding.setSubmittedBy(SCHOOL_REGISTRATION_PORTAL);
        onboarding.setUpdatedAt(now);
        appendAudit(onboarding, SCHOOL_REGISTRATION_PORTAL, "NEW", "PENDING", "Pilot demo request submitted for VidyaSetu Onboarding Team review", now);
        onboardingRepository.save(onboarding);

        return toRegistrationResponse(onboarding, "Registration Submitted successfully for pilot demo request.", "VidyaSetu Onboarding Team will schedule validation and guide the school through Approved, Pilot, Active, and Credentials Issued stages.");
    }

    public OnboardingStatusResponseDTO getStatus(String referenceId) {
        SchoolOnboardingRequest request = onboardingRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding reference not found."));
        return toStatusResponse(request);
    }

    public OnboardingStatusResponseDTO getStatusBySchoolId(String rawSchoolId) {
        String schoolId = normalizeSchoolId(rawSchoolId);
        if ("BRK1".equals(schoolId)) {
            return new OnboardingStatusResponseDTO("DEMO-BRK1", "BRK1", "BRK International School", "DEMO_TENANT", "ACTIVE",
                    "Demo school workspace is active for VidyaSetu validation.",
                    "Continue testing login, dashboards, and onboarding lifecycle. Final Excel import remains disabled for now.", true, false);
        }
        return onboardingRepository.findTopBySchoolIdOrderByUpdatedAtDesc(schoolId)
                .map(this::toStatusResponse)
                .orElseGet(() -> getNotStartedStatus(schoolId));
    }

    public OnboardingStatusResponseDTO getNotStartedStatus(String rawSchoolId) {
        String schoolId = normalizeSchoolId(rawSchoolId);
        return new OnboardingStatusResponseDTO(null, schoolId, "", "SCHOOL_ONBOARDING", "NOT_STARTED",
                "No onboarding request has been submitted for this school_id yet.",
                "Use Register School or Request Pilot Demo from the login screen to start onboarding.", false, false);
    }

    public List<OnboardingReviewItemDTO> getReviewQueue() {
        return onboardingRepository.findByStatusInOrderByUpdatedAtDesc(REVIEW_QUEUE_STATUSES).stream().map(this::toReviewItem).toList();
    }

    public boolean isLoginEnabledForSchoolId(String rawSchoolId) {
        String schoolId = normalizeSchoolId(rawSchoolId);
        if ("BRK1".equals(schoolId) || "DEMO".equals(schoolId)) {
            return true;
        }
        return onboardingRepository.findTopBySchoolIdOrderByUpdatedAtDesc(schoolId)
                .map(request -> "ACTIVE".equals(request.getStatus()))
                .orElse(false);
    }

    @Transactional
    public OnboardingStatusResponseDTO approveTenant(String referenceId, OnboardingStatusUpdateRequestDTO request) { return transition(referenceId, "APPROVED", request); }
    @Transactional
    public OnboardingStatusResponseDTO rejectTenant(String referenceId, OnboardingStatusUpdateRequestDTO request) { return transition(referenceId, "REJECTED", request); }
    @Transactional
    public OnboardingStatusResponseDTO markPilot(String referenceId, OnboardingStatusUpdateRequestDTO request) { return transition(referenceId, "PILOT", request); }
    @Transactional
    public OnboardingStatusResponseDTO activateTenant(String referenceId, OnboardingStatusUpdateRequestDTO request) { return transition(referenceId, "ACTIVE", request); }

    @Transactional
    public OnboardingStatusResponseDTO updateLifecycleStatus(String referenceId, OnboardingStatusUpdateRequestDTO request) {
        return transition(referenceId, normalizeStatus(request.getStatus()), request);
    }

    private OnboardingStatusResponseDTO transition(String referenceId, String nextStatus, OnboardingStatusUpdateRequestDTO request) {
        SchoolOnboardingRequest onboarding = onboardingRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding reference not found."));
        LocalDateTime now = LocalDateTime.now();
        validateTransition(onboarding.getStatus(), nextStatus);
        String fromStatus = onboarding.getStatus();
        onboarding.setStatus(nextStatus);
        onboarding.setReviewNotes(safeText(request == null ? null : request.getReviewNotes()));
        onboarding.setUpdatedAt(now);
        if ("APPROVED".equals(nextStatus)) { onboarding.setApprovedAt(now); onboarding.setApprovedBy(VIDYASETU_ONBOARDING_TEAM); }
        if ("PILOT".equals(nextStatus)) { onboarding.setPilotActivatedAt(now); onboarding.setPilotEnabledBy(VIDYASETU_ONBOARDING_TEAM); }
        if ("ACTIVE".equals(nextStatus)) {
            onboarding.setActivatedAt(now);
            onboarding.setActivatedBy(VIDYASETU_ONBOARDING_TEAM);
            appendAudit(onboarding, VIDYASETU_ONBOARDING_TEAM, "WORKSPACE_SETUP", "INITIALIZED", "Tenant workspace shell initialized for school_id " + normalizeSchoolId(onboarding.getSchoolId()) + "; import remains disabled until Excel onboarding validation", now);
        }
        if ("REJECTED".equals(nextStatus)) onboarding.setRejectedAt(now);
        appendAudit(onboarding, VIDYASETU_ONBOARDING_TEAM, fromStatus, nextStatus, onboarding.getReviewNotes(), now);
        onboardingRepository.save(onboarding);
        return toStatusResponse(onboarding);
    }


    @Transactional
    public ActivationPackageDTO generateActivationPackage(String referenceId) {
        SchoolOnboardingRequest onboarding = onboardingRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding reference not found."));
        if (!"ACTIVE".equals(onboarding.getStatus())) {
            throw new IllegalArgumentException("Activation package can be generated only after school workspace status is ACTIVE.");
        }
        if (onboarding.getSchoolId() == null || onboarding.getSchoolId().isBlank()) {
            throw new IllegalArgumentException("school_id is required before credential provisioning.");
        }

        LocalDateTime now = LocalDateTime.now();
        String schoolId = normalizeSchoolId(onboarding.getSchoolId());
        String adminUsername = schoolId.toLowerCase(Locale.ROOT) + ".admin";
        String principalUsername = schoolId.toLowerCase(Locale.ROOT) + ".principal";

        String adminPassword = onboarding.getAdminInitialPassword();
        if (adminPassword == null || adminPassword.isBlank()) adminPassword = generateInitialPassword(schoolId, "ADM");
        String principalPassword = onboarding.getPrincipalInitialPassword();
        if (principalPassword == null || principalPassword.isBlank()) principalPassword = generateInitialPassword(schoolId, "PRI");

        boolean adminCreated = provisionUser(adminUsername, adminPassword, "ADMIN", "School Admin", onboarding);
        boolean principalCreated = provisionUser(principalUsername, principalPassword, "PRINCIPAL", "Principal", onboarding);

        onboarding.setAdminUsername(adminUsername);
        onboarding.setAdminInitialPassword(adminPassword);
        onboarding.setPrincipalUsername(principalUsername);
        onboarding.setPrincipalInitialPassword(principalPassword);
        onboarding.setCredentialsIssuedBy(VIDYASETU_ONBOARDING_TEAM);
        if (onboarding.getCredentialsIssuedAt() == null) onboarding.setCredentialsIssuedAt(now);
        onboarding.setUpdatedAt(now);
        if (onboarding.getCredentialsIssuedAt() == null || adminCreated || principalCreated) {
            appendAudit(onboarding, VIDYASETU_ONBOARDING_TEAM, "ACTIVE", "CREDENTIALS_ISSUED", "First Admin and Principal credentials generated", now);
        }
        onboardingRepository.save(onboarding);

        return buildActivationPackage(onboarding, List.of(
                        new ActivationCredentialDTO("ADMIN", adminUsername, adminPassword, "School Admin", adminCreated),
                        new ActivationCredentialDTO("PRINCIPAL", principalUsername, principalPassword, "Principal", principalCreated)
                ), "Activation package is ready. First Admin and Principal accounts are provisioned for ERP login.",
                "Share these credentials securely with the approved school authority, then ask Admin and Principal to login using school_id " + schoolId + ".");
    }

    @Transactional
    public ActivationPackageDTO regenerateActivationCredentials(String referenceId) {
        SchoolOnboardingRequest onboarding = onboardingRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding reference not found."));
        if (!"ACTIVE".equals(onboarding.getStatus())) {
            throw new IllegalArgumentException("Credentials can be regenerated only after school workspace status is ACTIVE.");
        }
        if (onboarding.getSchoolId() == null || onboarding.getSchoolId().isBlank()) {
            throw new IllegalArgumentException("school_id is required before credential regeneration.");
        }

        LocalDateTime now = LocalDateTime.now();
        String schoolId = normalizeSchoolId(onboarding.getSchoolId());
        String adminUsername = schoolId.toLowerCase(Locale.ROOT) + ".admin";
        String principalUsername = schoolId.toLowerCase(Locale.ROOT) + ".principal";
        String adminPassword = generateInitialPassword(schoolId, "ADM");
        String principalPassword = generateInitialPassword(schoolId, "PRI");

        provisionUser(adminUsername, adminPassword, "ADMIN", "School Admin", onboarding);
        provisionUser(principalUsername, principalPassword, "PRINCIPAL", "Principal", onboarding);

        onboarding.setAdminUsername(adminUsername);
        onboarding.setAdminInitialPassword(adminPassword);
        onboarding.setPrincipalUsername(principalUsername);
        onboarding.setPrincipalInitialPassword(principalPassword);
        onboarding.setCredentialsIssuedBy(VIDYASETU_ONBOARDING_TEAM);
        onboarding.setCredentialsIssuedAt(now);
        onboarding.setUpdatedAt(now);
        appendAudit(onboarding, VIDYASETU_ONBOARDING_TEAM, "CREDENTIALS_ISSUED", "CREDENTIALS_REGENERATED", "Temporary Admin and Principal credentials regenerated; previous passwords are invalidated", now);
        onboardingRepository.save(onboarding);

        return buildActivationPackage(onboarding, List.of(
                        new ActivationCredentialDTO("ADMIN", adminUsername, adminPassword, "School Admin", false),
                        new ActivationCredentialDTO("PRINCIPAL", principalUsername, principalPassword, "Principal", false)
                ), "Credentials regenerated. Old temporary passwords are now invalid.",
                "Share the new credentials securely. First login must complete temporary password change before dashboard access.");
    }

    public ActivationPackageDTO getActivationPackage(String referenceId) {
        SchoolOnboardingRequest onboarding = onboardingRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding reference not found."));
        if (!"ACTIVE".equals(onboarding.getStatus())) {
            throw new IllegalArgumentException("Activation package is available only after school workspace status is ACTIVE.");
        }
        if (onboarding.getAdminUsername() == null || onboarding.getPrincipalUsername() == null) {
            return generateActivationPackage(referenceId);
        }
        return buildActivationPackage(onboarding, List.of(
                        new ActivationCredentialDTO("ADMIN", onboarding.getAdminUsername(), onboarding.getAdminInitialPassword(), "School Admin", false),
                        new ActivationCredentialDTO("PRINCIPAL", onboarding.getPrincipalUsername(), onboarding.getPrincipalInitialPassword(), "Principal", false)
                ), "Activation package is ready. Login is enabled for this school workspace.",
                "Share credentials securely and complete first login validation.");
    }


    private ActivationPackageDTO buildActivationPackage(SchoolOnboardingRequest onboarding, List<ActivationCredentialDTO> credentials, String message, String nextStep) {
        String schoolId = normalizeSchoolId(onboarding.getSchoolId());
        ActivationPackageDTO dto = new ActivationPackageDTO();
        dto.setReferenceId(onboarding.getReferenceId());
        dto.setSchoolId(schoolId);
        dto.setSchoolName(onboarding.getSchoolName());
        dto.setStatus(onboarding.getStatus());
        dto.setRegistrationDate(toIso(onboarding.getSubmittedAt()));
        dto.setActivatedAt(toIso(onboarding.getActivatedAt()));
        dto.setCredentialsIssuedAt(toIso(onboarding.getCredentialsIssuedAt()));
        dto.setLoginEnabled("ACTIVE".equals(onboarding.getStatus()) && onboarding.getCredentialsIssuedAt() != null);
        dto.setMessage(message);
        dto.setNextStep(nextStep);
        dto.setCredentials(credentials);
        dto.setWorkspaceSteps(workspaceSteps(onboarding));
        dto.setActivationChecklist(List.of(
                "Confirm school_id " + schoolId + " and school name before sharing credentials",
                "Share first Admin and Principal credentials only with the approved school authority",
                "Complete first login validation for both roles",
                "Ask school to change temporary passwords before real data import",
                "Keep Excel import disabled until final onboarding sheet validation"
        ));
        dto.setImportPreparationChecklist(List.of(
                "Prepare final Classes, Sections, Teachers, Students, Subjects, TeacherPools, and TeacherAssignments sheets",
                "Validate student and teacher counts against expected onboarding size",
                "Run preview/validation before committing import",
                "Commit real Excel data only after workspace activation and credential handover"
        ));
        dto.setStatusSummary(toStatusResponse(onboarding));
        return dto;
    }

    private List<WorkspaceProvisioningStepDTO> workspaceSteps(SchoolOnboardingRequest onboarding) {
        boolean active = "ACTIVE".equals(onboarding.getStatus());
        boolean credentialsIssued = onboarding.getCredentialsIssuedAt() != null;
        return List.of(
                new WorkspaceProvisioningStepDTO("TENANT", "Tenant school_id reserved", onboarding.getSchoolId() == null || onboarding.getSchoolId().isBlank() ? "PENDING" : "DONE", "Immutable 4-character school_id: " + normalizeSchoolId(onboarding.getSchoolId())),
                new WorkspaceProvisioningStepDTO("PROFILE", "School profile initialized", active ? "DONE" : "WAITING", onboarding.getSchoolName() + " profile is attached to the workspace shell"),
                new WorkspaceProvisioningStepDTO("RBAC", "Admin/Principal RBAC prepared", active ? "DONE" : "WAITING", "First operational roles are restricted to school_id " + normalizeSchoolId(onboarding.getSchoolId())),
                new WorkspaceProvisioningStepDTO("CREDENTIALS", "First credentials issued", credentialsIssued ? "DONE" : "WAITING", credentialsIssued ? "Temporary credentials are available in this package" : "Generate package after Active status"),
                new WorkspaceProvisioningStepDTO("IMPORT", "Excel onboarding import", "LOCKED", "Real Excel import remains locked until school validates the activation package")
        );
    }

    private boolean provisionUser(String username, String rawPassword, String role, String displayName, SchoolOnboardingRequest onboarding) {
        String schoolId = normalizeSchoolId(onboarding.getSchoolId());
        var existing = appUserRepository.findByUsernameAndSchoolCodeIgnoreCase(username, schoolId);
        if (existing.isPresent()) {
            AppUser user = existing.get();
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setCredentialsActive(true);
            user.setForcePasswordChange(true);
            user.setSchoolName(onboarding.getSchoolName());
            user.setDisplayName(displayName);
            appUserRepository.save(user);
            return false;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setSchoolId(1L);
        user.setSchoolCode(schoolId);
        user.setDisplayName(displayName);
        user.setSchoolName(onboarding.getSchoolName());
        user.setCredentialsActive(true);
        user.setForcePasswordChange(true);
        appUserRepository.save(user);
        return true;
    }

    private void validateTransition(String fromStatus, String nextStatus) {
        if (!List.of("PENDING", "APPROVED", "PILOT", "ACTIVE", "REJECTED").contains(nextStatus)) {
            throw new IllegalArgumentException("Status must be PENDING, APPROVED, PILOT, ACTIVE, or REJECTED.");
        }
        if (nextStatus.equals(fromStatus)) return;
        boolean valid = switch (fromStatus) {
            case "PENDING" -> List.of("APPROVED", "REJECTED").contains(nextStatus);
            case "APPROVED" -> List.of("PILOT", "REJECTED").contains(nextStatus);
            case "PILOT" -> List.of("ACTIVE", "REJECTED").contains(nextStatus);
            case "ACTIVE" -> false;
            case "REJECTED" -> false;
            case "RESERVED" -> "PENDING".equals(nextStatus);
            default -> false;
        };
        if (!valid) throw new IllegalArgumentException("Invalid onboarding transition: " + fromStatus + " -> " + nextStatus + ". Expected flow is PENDING -> APPROVED -> PILOT -> ACTIVE.");
    }

    private OnboardingStatusResponseDTO toStatusResponse(SchoolOnboardingRequest request) {
        String status = request.getStatus();
        boolean loginEnabled = "ACTIVE".equals(status);
        OnboardingStatusResponseDTO dto = new OnboardingStatusResponseDTO(request.getReferenceId(), request.getSchoolId(), request.getSchoolName(), request.getRequestType(), status,
                statusMessage(status), nextStep(status), loginEnabled, false);
        dto.setRegistrationDate(toIso(request.getSubmittedAt()));
        dto.setSubmittedAt(toIso(request.getSubmittedAt()));
        dto.setApprovedAt(toIso(request.getApprovedAt()));
        dto.setPilotActivatedAt(toIso(request.getPilotActivatedAt()));
        dto.setActivatedAt(toIso(request.getActivatedAt()));
        dto.setSubmittedBy(submittedActor(request.getSubmittedBy()));
        dto.setApprovedBy(request.getApprovedBy());
        dto.setPilotEnabledBy(request.getPilotEnabledBy());
        dto.setActivatedBy(request.getActivatedBy());
        dto.setCredentialsIssuedBy(request.getCredentialsIssuedBy());
        dto.setCredentialsIssuedAt(toIso(request.getCredentialsIssuedAt()));
        dto.setStatusHistory(normalizeAuditHistory(request.getStatusHistory()));
        return dto;
    }

    private OnboardingReviewItemDTO toReviewItem(SchoolOnboardingRequest request) {
        OnboardingReviewItemDTO dto = new OnboardingReviewItemDTO();
        dto.setReferenceId(request.getReferenceId()); dto.setSchoolId(request.getSchoolId()); dto.setSchoolName(request.getSchoolName());
        dto.setRequestType(request.getRequestType()); dto.setStatus(request.getStatus()); dto.setContactPerson(request.getContactPerson());
        dto.setContactPhone(request.getContactPhone()); dto.setContactEmail(request.getContactEmail()); dto.setExpectedStudents(request.getExpectedStudents());
        dto.setExpectedTeachers(request.getExpectedTeachers()); dto.setCity(request.getCity()); dto.setState(request.getState());
        dto.setSubmittedAt(toIso(request.getSubmittedAt())); dto.setUpdatedAt(toIso(request.getUpdatedAt())); dto.setApprovedAt(toIso(request.getApprovedAt()));
        dto.setPilotActivatedAt(toIso(request.getPilotActivatedAt())); dto.setActivatedAt(toIso(request.getActivatedAt())); dto.setRejectedAt(toIso(request.getRejectedAt()));
        dto.setReviewNotes(request.getReviewNotes()); dto.setSubmittedBy(submittedActor(request.getSubmittedBy())); dto.setApprovedBy(request.getApprovedBy()); dto.setPilotEnabledBy(request.getPilotEnabledBy()); dto.setActivatedBy(request.getActivatedBy()); dto.setCredentialsIssuedBy(request.getCredentialsIssuedBy()); dto.setCredentialsIssuedAt(toIso(request.getCredentialsIssuedAt())); dto.setStatusHistory(normalizeAuditHistory(request.getStatusHistory()));
        return dto;
    }

    private SchoolRegistrationResponseDTO toRegistrationResponse(SchoolOnboardingRequest request, String message, String nextStep) {
        return new SchoolRegistrationResponseDTO(request.getReferenceId(), request.getSchoolId(), request.getSchoolName(), request.getStatus(), message, nextStep);
    }

    private void appendAudit(SchoolOnboardingRequest request, String actor, String fromStatus, String toStatus, String note, LocalDateTime at) {
        String cleanNote = safeText(note).replace("|", "/").replace("\n", " ");
        String entry = at + " | " + actor + " | " + fromStatus + " -> " + toStatus + (cleanNote.isBlank() ? "" : " | " + cleanNote);
        String existing = request.getStatusHistory();
        String history = (existing == null || existing.isBlank()) ? entry : existing + "\n" + entry;
        request.setStatusHistory(history.length() > 3900 ? history.substring(history.length() - 3900) : history);
    }


    private String normalizeAuditHistory(String history) {
        if (history == null || history.isBlank()) {
            return history;
        }
        return history
                .replace("registration submitted for Admin/Principal review", "Registration submitted for VidyaSetu Onboarding Team review")
                .replace("registration submitted for admin/principal review", "Registration submitted for VidyaSetu Onboarding Team review")
                .replace("Admin/Principal review", "VidyaSetu Onboarding Team review")
                .replace("admin/principal review", "VidyaSetu Onboarding Team review");
    }

    private boolean isSystemReservedSchoolId(String schoolId) { return List.of("BRK1", "DEMO", "TEST").contains(schoolId); }
    private String normalizeSchoolId(String value) { return (value == null ? "" : value.trim()).toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", ""); }
    private String normalizeStatus(String value) { return safeText(value).toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'); }
    private String safeText(String value) { return value == null ? "" : value.trim(); }
    private String toIso(LocalDateTime value) { return value == null ? null : value.toString(); }
    private String defaultActor(String actor) { return actor == null || actor.isBlank() ? VIDYASETU_ONBOARDING_TEAM : actor; }
    private String submittedActor(String actor) { return actor == null || actor.isBlank() || VIDYASETU_ONBOARDING_TEAM.equals(actor) ? SCHOOL_REGISTRATION_PORTAL : actor; }
    private String generateInitialPassword(String schoolId, String prefix) {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(prefix).append("-").append(schoolId).append("-");
        for (int i = 0; i < 5; i++) sb.append(alphabet.charAt(SECURE_RANDOM.nextInt(alphabet.length())));
        return sb.toString();
    }
    private String buildReferenceId(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return prefix + "-" + date + "-" + suffix;
    }
    private String statusMessage(String status) {
        return switch (status) {
            case "RESERVED" -> "school_id is reserved but registration details are not completed yet.";
            case "PENDING" -> "Your registration request is under review by the VidyaSetu Onboarding Team.";
            case "APPROVED" -> "Your school registration is approved. VidyaSetu is preparing the ERP workspace.";
            case "PILOT" -> "Pilot workspace validation is enabled. Login credentials will be issued after activation.";
            case "ACTIVE" -> "Your school workspace is active. ERP login is enabled after credentials are issued.";
            case "REJECTED" -> "Onboarding request was rejected or cancelled.";
            default -> "Onboarding status is available.";
        };
    }
    private String nextStep(String status) {
        return switch (status) {
            case "RESERVED" -> "Complete school registration using the reserved school_id.";
            case "PENDING" -> "VidyaSetu Onboarding Team will review your registration and move it through the onboarding stages.";
            case "APPROVED" -> "VidyaSetu Onboarding Team will complete workspace setup and enable pilot validation.";
            case "PILOT" -> "VidyaSetu Onboarding Team will validate the pilot workspace and activate login when ready.";
            case "ACTIVE" -> "Generate the activation package, share credentials securely, and complete first Admin/Principal login validation.";
            case "REJECTED" -> "Contact school again or submit a new onboarding request.";
            default -> "Continue onboarding review.";
        };
    }
}
