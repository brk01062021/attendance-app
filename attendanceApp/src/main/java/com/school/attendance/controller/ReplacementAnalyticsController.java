package com.school.attendance.controller;

import com.school.attendance.dto.TeacherWorkloadProtectionDTO;
import com.school.attendance.service.TeacherLeavePlanningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/teacher-replacements")
public class ReplacementAnalyticsController {

    private final TeacherLeavePlanningService teacherLeavePlanningService;

    public ReplacementAnalyticsController(TeacherLeavePlanningService teacherLeavePlanningService) {
        this.teacherLeavePlanningService = teacherLeavePlanningService;
    }

    @GetMapping("/load-summary")
    public List<TeacherWorkloadProtectionDTO> loadSummary(@RequestParam String fromDate, @RequestParam String toDate) {
        return teacherLeavePlanningService.workloadProtection(fromDate, toDate);
    }
}
