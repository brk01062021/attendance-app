package com.school.attendance.service.onboarding;

import com.school.attendance.dto.onboarding.OnboardingReviewItemDTO;
import com.school.attendance.dto.onboarding.OnboardingStatusResponseDTO;
import com.school.attendance.dto.onboarding.OnboardingStatusUpdateRequestDTO;
import com.school.attendance.dto.onboarding.PilotDemoRequestDTO;
import com.school.attendance.dto.onboarding.SchoolIdAvailabilityResponseDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationRequestDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationResponseDTO;
import com.school.attendance.entity.SchoolOnboardingRequest;
import com.school.attendance.repository.SchoolOnboardingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SchoolRegistrationService {
    private static final String SCHOOL_ID_PATTERN = "^[A-Z0-9]{4}$";
    private static final List<String> BLOCKING_SCHOOL_ID_STATUSES = List.of("RESERVED", "PENDING", "APPROVED", "PILOT", "ACTIVE");
    private static final List<String> REVIEW_QUEUE_STATUSES = List.of("PENDING", "APPROVED", "PILOT", "ACTIVE", "RESERVED", "REJECTED");
    private final SchoolOnboardingRequestRepository onboardingRepository;

    public SchoolRegistrationService(SchoolOnboardingRequestRepository onboardingRepository) {
        this.onboardingRepository = onboardingRepository;
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
        onboarding.setUpdatedAt(now);
        appendAudit(onboarding, "SCHOOL", fromStatus, "PENDING", "registration submitted for Admin/Principal review", now);
        onboardingRepository.save(onboarding);

        return toRegistrationResponse(onboarding, "Registration submitted and moved to Pending review.", "Admin/Principal review can approve the tenant, mark pilot, or activate after validation. Final Excel import remains disabled.");
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
        onboarding.setUpdatedAt(now);
        appendAudit(onboarding, "SCHOOL", "NEW", "PENDING", "pilot demo request submitted", now);
        onboardingRepository.save(onboarding);

        return toRegistrationResponse(onboarding, "Pilot demo request moved to Pending review.", "Schedule demo, confirm school size, then guide the school through registration and sample-data onboarding.");
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
                    "Demo tenant is active for VidyaSetu validation.",
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
        if ("APPROVED".equals(nextStatus)) onboarding.setApprovedAt(now);
        if ("PILOT".equals(nextStatus)) onboarding.setPilotActivatedAt(now);
        if ("ACTIVE".equals(nextStatus)) onboarding.setActivatedAt(now);
        if ("REJECTED".equals(nextStatus)) onboarding.setRejectedAt(now);
        appendAudit(onboarding, "ADMIN_PRINCIPAL", fromStatus, nextStatus, onboarding.getReviewNotes(), now);
        onboardingRepository.save(onboarding);
        return toStatusResponse(onboarding);
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
        return new OnboardingStatusResponseDTO(request.getReferenceId(), request.getSchoolId(), request.getSchoolName(), request.getRequestType(), status,
                statusMessage(status), nextStep(status), loginEnabled, false);
    }

    private OnboardingReviewItemDTO toReviewItem(SchoolOnboardingRequest request) {
        OnboardingReviewItemDTO dto = new OnboardingReviewItemDTO();
        dto.setReferenceId(request.getReferenceId()); dto.setSchoolId(request.getSchoolId()); dto.setSchoolName(request.getSchoolName());
        dto.setRequestType(request.getRequestType()); dto.setStatus(request.getStatus()); dto.setContactPerson(request.getContactPerson());
        dto.setContactPhone(request.getContactPhone()); dto.setContactEmail(request.getContactEmail()); dto.setExpectedStudents(request.getExpectedStudents());
        dto.setExpectedTeachers(request.getExpectedTeachers()); dto.setCity(request.getCity()); dto.setState(request.getState());
        dto.setSubmittedAt(toIso(request.getSubmittedAt())); dto.setUpdatedAt(toIso(request.getUpdatedAt())); dto.setApprovedAt(toIso(request.getApprovedAt()));
        dto.setPilotActivatedAt(toIso(request.getPilotActivatedAt())); dto.setActivatedAt(toIso(request.getActivatedAt())); dto.setRejectedAt(toIso(request.getRejectedAt()));
        dto.setReviewNotes(request.getReviewNotes()); dto.setStatusHistory(request.getStatusHistory());
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

    private boolean isSystemReservedSchoolId(String schoolId) { return List.of("BRK1", "DEMO", "TEST").contains(schoolId); }
    private String normalizeSchoolId(String value) { return (value == null ? "" : value.trim()).toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", ""); }
    private String normalizeStatus(String value) { return safeText(value).toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'); }
    private String safeText(String value) { return value == null ? "" : value.trim(); }
    private String toIso(LocalDateTime value) { return value == null ? null : value.toString(); }
    private String buildReferenceId(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return prefix + "-" + date + "-" + suffix;
    }
    private String statusMessage(String status) {
        return switch (status) {
            case "RESERVED" -> "school_id is reserved but registration details are not completed yet.";
            case "PENDING" -> "Onboarding request is pending review.";
            case "APPROVED" -> "Onboarding request is approved. Tenant setup can proceed.";
            case "PILOT" -> "Pilot tenant is enabled for controlled validation. Login remains locked until Active.";
            case "ACTIVE" -> "Tenant is active for normal login and school operations.";
            case "REJECTED" -> "Onboarding request was rejected or cancelled.";
            default -> "Onboarding status is available.";
        };
    }
    private String nextStep(String status) {
        return switch (status) {
            case "RESERVED" -> "Complete school registration using the reserved school_id.";
            case "PENDING" -> "Admin/Principal must review and approve or reject this request.";
            case "APPROVED" -> "Move tenant to Pilot after setup validation. Final Excel import remains disabled.";
            case "PILOT" -> "Validate pilot setup, then activate tenant to enable login.";
            case "ACTIVE" -> "Use normal VidyaSetu login and school operations.";
            case "REJECTED" -> "Contact school again or submit a new onboarding request.";
            default -> "Continue onboarding review.";
        };
    }
}
