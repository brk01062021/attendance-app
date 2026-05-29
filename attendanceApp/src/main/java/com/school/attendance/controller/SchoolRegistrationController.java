package com.school.attendance.controller;

import com.school.attendance.dto.onboarding.OnboardingReviewItemDTO;
import com.school.attendance.dto.onboarding.OnboardingStatusResponseDTO;
import com.school.attendance.dto.onboarding.OnboardingStatusUpdateRequestDTO;
import com.school.attendance.dto.onboarding.PilotDemoRequestDTO;
import com.school.attendance.dto.onboarding.SchoolIdAvailabilityResponseDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationRequestDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationResponseDTO;
import com.school.attendance.service.onboarding.SchoolRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/school-registration", "/api/school-registration"})
public class SchoolRegistrationController {
    private final SchoolRegistrationService schoolRegistrationService;

    public SchoolRegistrationController(SchoolRegistrationService schoolRegistrationService) {
        this.schoolRegistrationService = schoolRegistrationService;
    }

    @GetMapping("/school-id/check")
    public ResponseEntity<SchoolIdAvailabilityResponseDTO> checkSchoolId(@RequestParam String schoolId) {
        return ResponseEntity.ok(schoolRegistrationService.checkSchoolId(schoolId));
    }

    @PostMapping("/school-id/reserve")
    public ResponseEntity<SchoolIdAvailabilityResponseDTO> reserveSchoolId(@RequestParam String schoolId) {
        return ResponseEntity.ok(schoolRegistrationService.reserveSchoolId(schoolId));
    }

    @PostMapping("/register")
    public ResponseEntity<SchoolRegistrationResponseDTO> registerSchool(@Valid @RequestBody SchoolRegistrationRequestDTO request) {
        return ResponseEntity.ok(schoolRegistrationService.registerSchool(request));
    }

    @PostMapping("/pilot-demo/request")
    public ResponseEntity<SchoolRegistrationResponseDTO> requestPilotDemo(@Valid @RequestBody PilotDemoRequestDTO request) {
        return ResponseEntity.ok(schoolRegistrationService.requestPilotDemo(request));
    }

    @GetMapping("/status")
    public ResponseEntity<OnboardingStatusResponseDTO> getStatus(
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String schoolId) {
        if (schoolId != null && !schoolId.isBlank()) {
            return ResponseEntity.ok(schoolRegistrationService.getStatusBySchoolId(schoolId));
        }
        if (referenceId != null && !referenceId.isBlank()) {
            return ResponseEntity.ok(schoolRegistrationService.getStatus(referenceId));
        }
        return ResponseEntity.ok(schoolRegistrationService.getNotStartedStatus(null));
    }

    @GetMapping("/status/by-school-id")
    public ResponseEntity<OnboardingStatusResponseDTO> getStatusBySchoolId(@RequestParam String schoolId) {
        return ResponseEntity.ok(schoolRegistrationService.getStatusBySchoolId(schoolId));
    }

    @GetMapping("/review-queue")
    public ResponseEntity<List<OnboardingReviewItemDTO>> getReviewQueue() {
        return ResponseEntity.ok(schoolRegistrationService.getReviewQueue());
    }

    @PostMapping("/review/{referenceId}/status")
    public ResponseEntity<OnboardingStatusResponseDTO> updateLifecycleStatus(
            @PathVariable String referenceId,
            @Valid @RequestBody OnboardingStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(schoolRegistrationService.updateLifecycleStatus(referenceId, request));
    }
}
