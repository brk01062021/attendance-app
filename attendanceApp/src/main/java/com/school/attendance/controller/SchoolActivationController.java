package com.school.attendance.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.dto.SchoolActivationRequestDTO;
import com.school.attendance.dto.WorkspaceActivationAuditDTO;
import com.school.attendance.dto.WorkspaceActivationSummaryDTO;
import com.school.attendance.service.WorkspaceActivationService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/school-activation", "/api/school-activation"})
public class SchoolActivationController {

    private final WorkspaceActivationService workspaceActivationService;

    public SchoolActivationController(WorkspaceActivationService workspaceActivationService) {
        this.workspaceActivationService = workspaceActivationService;
    }

    @PostMapping({"/execute", "/activate"})
    public ApiResponse<WorkspaceActivationSummaryDTO> execute(@RequestParam String schoolId,
                                                              @RequestBody(required = false) SchoolActivationRequestDTO request) {
        return ApiResponse.success("School activation execution completed", workspaceActivationService.activate(schoolId, request));
    }

    @GetMapping({"/success-summary", "/summary"})
    public ApiResponse<WorkspaceActivationSummaryDTO> successSummary(@RequestParam String schoolId) {
        return ApiResponse.success("School activation success dashboard loaded", workspaceActivationService.getSummary(schoolId));
    }

    @GetMapping("/reporting")
    public ApiResponse<Map<String, Object>> reporting(@RequestParam String schoolId) {
        WorkspaceActivationSummaryDTO summary = workspaceActivationService.getSummary(schoolId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("schoolId", summary.getSchoolId());
        data.put("schoolName", summary.getSchoolName());
        data.put("academicYear", summary.getAcademicYear());
        data.put("activationStatus", summary.getActivationStatus());
        data.put("readinessPercent", summary.getReadinessPercent());
        data.put("tenantActive", summary.isTenantActive());
        data.put("readyForActivation", summary.isReadyForActivation());
        data.put("committedWorkbookCount", summary.getCommittedWorkbookCount());
        data.put("lastWorkbookCommittedAt", summary.getLastWorkbookCommittedAt());
        data.put("activatedBy", summary.getActivatedBy());
        data.put("activatedAt", summary.getActivatedAt());
        data.put("healthItems", summary.getHealthItems());
        data.put("auditTrail", summary.getAuditTrail());
        return ApiResponse.success("Principal/Admin activation reporting loaded", data);
    }

    @GetMapping({"/notifications", "/activation-notifications"})
    public ApiResponse<List<WorkspaceActivationAuditDTO>> notifications(@RequestParam String schoolId) {
        return ApiResponse.success("Activation notification center loaded", workspaceActivationService.getSummary(schoolId).getAuditTrail());
    }
}
