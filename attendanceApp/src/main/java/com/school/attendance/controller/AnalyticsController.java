package com.school.attendance.controller;

import com.school.attendance.dto.AnalyticsSummaryDTO;
import com.school.attendance.dto.AttendanceTrendDTO;
import com.school.attendance.dto.ClassAttendanceTrendDTO;
import com.school.attendance.dto.TeacherReplacementTrendDTO;
import com.school.attendance.dto.SectionAnalyticsDTO;
import com.school.attendance.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public AnalyticsSummaryDTO getSummary(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return analyticsService.getSummary(startDate, endDate);
    }

    @GetMapping("/attendance-trend")
    public List<AttendanceTrendDTO> getAttendanceTrend(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return analyticsService.getAttendanceTrend(startDate, endDate);
    }

    @GetMapping("/class-attendance-trend")
    public List<ClassAttendanceTrendDTO> getClassAttendanceTrend(
            @RequestParam LocalDate date
    ) {
        return analyticsService.getClassAttendanceTrend(date);
    }


    @GetMapping("/attendance/monthly")
    public List<AttendanceTrendDTO> getMonthlyAttendanceTrend(
            @RequestParam String month,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section
    ) {
        return analyticsService.getMonthlyAttendanceTrend(month, className, section);
    }

    @GetMapping("/class-comparison/monthly")
    public List<ClassAttendanceTrendDTO> getMonthlyClassComparison(
            @RequestParam String month
    ) {
        return analyticsService.getMonthlyClassComparison(month);
    }


    @GetMapping("/section-comparison")
    public List<SectionAnalyticsDTO> getSectionComparison(
            @RequestParam String month,
            @RequestParam(required = false) String className
    ) {
        return analyticsService.getMonthlySectionComparison(month, className);
    }

    @GetMapping("/replacement-trend")
    public List<TeacherReplacementTrendDTO> getReplacementTrend(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return analyticsService.getReplacementTrend(startDate, endDate);
    }
}