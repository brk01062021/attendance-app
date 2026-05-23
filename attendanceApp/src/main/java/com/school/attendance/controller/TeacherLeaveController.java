package com.school.attendance.controller;

import com.school.attendance.dto.ReplacementRecommendationDTO;
import com.school.attendance.dto.TeacherLeaveRequestDTO;
import com.school.attendance.entity.TeacherLeaveEnquiry;
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

    @PostMapping("/enquiry")
    public Map<String, Object> submitLeaveEnquiry(@RequestBody TeacherLeaveRequestDTO request) {
        return teacherLeavePlanningService.submitLeaveEnquiry(request);
    }

    @PostMapping("/submit")
    public Map<String, Object> submitLeave(@RequestBody TeacherLeaveRequestDTO request) {
        return teacherLeavePlanningService.submitLeave(request);
    }

    @GetMapping("/enquiry/history/{teacherId}")
    public List<TeacherLeaveEnquiry> teacherLeaveHistory(@PathVariable Long teacherId) {
        return teacherLeavePlanningService.teacherLeaveHistory(teacherId);
    }

    @GetMapping("/admin/enquiries")
    public List<TeacherLeaveEnquiry> pendingLeaveEnquiries(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        return teacherLeavePlanningService.pendingLeaveEnquiries(fromDate, toDate);
    }

    @PostMapping("/admin/enquiries/{enquiryId}/approve")
    public Map<String, Object> approveLeaveEnquiry(
            @PathVariable Long enquiryId,
            @RequestParam(required = false) String adminRemarks
    ) {
        return teacherLeavePlanningService.approveLeaveEnquiry(enquiryId, adminRemarks);
    }

    @PostMapping("/admin/enquiries/{enquiryId}/reject")
    public Map<String, Object> rejectLeaveEnquiry(
            @PathVariable Long enquiryId,
            @RequestParam(required = false) String adminRemarks
    ) {
        return teacherLeavePlanningService.rejectLeaveEnquiry(enquiryId, adminRemarks);
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
