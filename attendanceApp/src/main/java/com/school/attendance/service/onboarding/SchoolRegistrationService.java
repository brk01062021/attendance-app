package com.school.attendance.service.onboarding;

import com.school.attendance.dto.onboarding.PilotDemoRequestDTO;
import com.school.attendance.dto.onboarding.SchoolIdAvailabilityResponseDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationRequestDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SchoolRegistrationService {
    private static final String SCHOOL_ID_PATTERN = "^[A-Z0-9]{4}$";
    private final Set<String> reservedSchoolIds = ConcurrentHashMap.newKeySet();
    private final Map<String, SchoolRegistrationResponseDTO> registrations = new ConcurrentHashMap<>();
    private final Map<String, SchoolRegistrationResponseDTO> pilotRequests = new ConcurrentHashMap<>();

    public SchoolRegistrationService() {
        reservedSchoolIds.add("BRK1");
        reservedSchoolIds.add("DEMO");
        reservedSchoolIds.add("TEST");
    }

    public SchoolIdAvailabilityResponseDTO checkSchoolId(String rawSchoolId) {
        String schoolId = normalizeSchoolId(rawSchoolId);
        if (!schoolId.matches(SCHOOL_ID_PATTERN)) {
            return new SchoolIdAvailabilityResponseDTO(schoolId, false, "INVALID", "School ID must be exactly 4 uppercase letters/numbers.");
        }
        boolean available = !reservedSchoolIds.contains(schoolId);
        return new SchoolIdAvailabilityResponseDTO(
                schoolId,
                available,
                available ? "AVAILABLE" : "RESERVED",
                available ? "School ID is available for registration." : "School ID is already reserved or active."
        );
    }

    public SchoolIdAvailabilityResponseDTO reserveSchoolId(String rawSchoolId) {
        SchoolIdAvailabilityResponseDTO availability = checkSchoolId(rawSchoolId);
        if (!availability.isAvailable()) {
            return availability;
        }
        reservedSchoolIds.add(availability.getSchoolId());
        return new SchoolIdAvailabilityResponseDTO(
                availability.getSchoolId(),
                false,
                "RESERVED",
                "School ID reserved for onboarding. Continue registration to complete the request."
        );
    }

    public SchoolRegistrationResponseDTO registerSchool(SchoolRegistrationRequestDTO request) {
        String schoolId = normalizeSchoolId(request.getRequestedSchoolId());
        SchoolIdAvailabilityResponseDTO availability = checkSchoolId(schoolId);
        if (!availability.isAvailable()) {
            throw new IllegalArgumentException(availability.getMessage());
        }
        reservedSchoolIds.add(schoolId);
        String referenceId = buildReferenceId("REG");
        SchoolRegistrationResponseDTO response = new SchoolRegistrationResponseDTO(
                referenceId,
                schoolId,
                safeText(request.getSchoolName()),
                "REGISTRATION_RECEIVED",
                "School registration foundation saved. Final Excel import is intentionally not used at this stage.",
                "VidyaSetu team can review school details, verify contact information, and activate pilot onboarding when ready."
        );
        registrations.put(referenceId, response);
        return response;
    }

    public SchoolRegistrationResponseDTO requestPilotDemo(PilotDemoRequestDTO request) {
        String referenceId = buildReferenceId("PILOT");
        SchoolRegistrationResponseDTO response = new SchoolRegistrationResponseDTO(
                referenceId,
                null,
                safeText(request.getSchoolName()),
                "PILOT_DEMO_REQUESTED",
                "Pilot demo request saved for follow-up.",
                "Schedule demo, confirm school size, then guide the school through registration and sample-data onboarding."
        );
        pilotRequests.put(referenceId, response);
        return response;
    }

    private String normalizeSchoolId(String value) {
        return (value == null ? "" : value.trim()).toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildReferenceId(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return prefix + "-" + date + "-" + suffix;
    }
}
