package com.school.attendance.controller;

import com.school.attendance.dto.ReplacementRecommendationDTO;
import com.school.attendance.dto.TeacherLeaveRequestDTO;
import com.school.attendance.entity.TeacherSchedule;
import com.school.attendance.service.TeacherLeavePlanningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher-leave")
public class TeacherLeaveController {

    private final TeacherLeavePlanningService teacherLeavePlanningService;

    public TeacherLeaveController(TeacherLeavePlanningService teacherLeavePlanningService) {
        this.teacherLeavePlanningService = teacherLeavePlanningService;
    }

    @PostMapping("/preview-replacements")
    public List<ReplacementRecommendationDTO> previewReplacements(@RequestBody TeacherLeaveRequestDTO request) {
        return teacherLeavePlanningService.previewReplacements(request);
    }

    @PostMapping("/submit")
    public Map<String, Object> submitLeave(@RequestBody TeacherLeaveRequestDTO request) {
        return teacherLeavePlanningService.submitLeave(request);
    }

    @GetMapping("/admin/pending")
    public List<TeacherSchedule> pendingApprovals(@RequestParam String fromDate, @RequestParam String toDate) {
        return teacherLeavePlanningService.pendingApprovals(fromDate, toDate);
    }

    @PostMapping("/admin/approve")
    public Map<String, Object> approveLeave(
            @RequestParam Long scheduleId,
            @RequestParam(required = false) Long replacementTeacherId,
            @RequestParam(required = false) String replacementTeacherName
    ) {
        return teacherLeavePlanningService.approveLeave(scheduleId, replacementTeacherId, replacementTeacherName);
    }
}
