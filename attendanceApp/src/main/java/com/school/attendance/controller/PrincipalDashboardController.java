package com.school.attendance.controller;

import com.school.attendance.dto.ClassComparisonDTO;
import com.school.attendance.dto.PrincipalDashboardSummaryDTO;
import com.school.attendance.dto.PrincipalRiskAlertDTO;
import com.school.attendance.service.PrincipalDashboardService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import com.school.attendance.service.PrincipalDashboardService;
import java.util.List;

@RestController
@RequestMapping("/principal/dashboard")
@CrossOrigin(origins = "*")
public class PrincipalDashboardController {

    private final PrincipalDashboardService principalDashboardService;

    public PrincipalDashboardController(PrincipalDashboardService principalDashboardService) {
        this.principalDashboardService = principalDashboardService;
    }

    @GetMapping("/summary")
    public PrincipalDashboardSummaryDTO getSummary(@RequestParam(required = false) LocalDate date) {
        return principalDashboardService.getSummary(date);
    }

    @GetMapping("/risk-alerts")
    public List<PrincipalRiskAlertDTO> getRiskAlerts(@RequestParam(required = false) String month) {
        return principalDashboardService.getRiskAlerts(month);
    }

    @GetMapping("/class-comparison")
    public List<ClassComparisonDTO> getClassComparison(
            @RequestParam String month,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String classA,
            @RequestParam(required = false) String classB
    ) {
        return principalDashboardService.getClassComparison(month, className, section, classA, classB);
    }
}
