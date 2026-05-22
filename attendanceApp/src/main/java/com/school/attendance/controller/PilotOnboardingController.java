package com.school.attendance.controller;

import com.school.attendance.dto.PilotOnboardingSummaryDTO;
import com.school.attendance.service.PilotOnboardingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/pilot-onboarding")
public class PilotOnboardingController {

    private final PilotOnboardingService pilotOnboardingService;

    public PilotOnboardingController(PilotOnboardingService pilotOnboardingService) {
        this.pilotOnboardingService = pilotOnboardingService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "pilot-onboarding",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/summary")
    public PilotOnboardingSummaryDTO summary(@RequestParam(defaultValue = "1") Long schoolId) {
        return pilotOnboardingService.getPilotSummary(schoolId);
    }
}
