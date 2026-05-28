package com.school.attendance.controller;

import com.school.attendance.dto.onboarding.PilotDemoRequestDTO;
import com.school.attendance.dto.onboarding.SchoolIdAvailabilityResponseDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationRequestDTO;
import com.school.attendance.dto.onboarding.SchoolRegistrationResponseDTO;
import com.school.attendance.service.onboarding.SchoolRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/school-registration")
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
}
