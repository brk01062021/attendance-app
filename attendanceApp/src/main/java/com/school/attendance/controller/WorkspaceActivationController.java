package com.school.attendance.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.dto.SchoolActivationRequestDTO;
import com.school.attendance.dto.WorkspaceActivationAuditDTO;
import com.school.attendance.dto.WorkspaceActivationSummaryDTO;
import com.school.attendance.dto.WorkspaceHealthItemDTO;
import com.school.attendance.service.WorkspaceActivationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/workspace-activation", "/api/workspace-activation"})
public class WorkspaceActivationController {
    private final WorkspaceActivationService service;

    public WorkspaceActivationController(WorkspaceActivationService service) {
        this.service = service;
    }

    @GetMapping({"/summary", "/health"})
    public ApiResponse<WorkspaceActivationSummaryDTO> summary(@RequestParam String schoolId) {
        return ApiResponse.success("Workspace activation health loaded", service.getSummary(schoolId));
    }

    @GetMapping("/audit-trail")
    public ApiResponse<List<WorkspaceActivationAuditDTO>> auditTrail(@RequestParam String schoolId) {
        return ApiResponse.success("Workspace activation audit trail loaded", service.getSummary(schoolId).getAuditTrail());
    }

    @GetMapping("/health-items")
    public ApiResponse<List<WorkspaceHealthItemDTO>> healthItems(@RequestParam String schoolId) {
        return ApiResponse.success("Workspace health items loaded", service.getSummary(schoolId).getHealthItems());
    }

    @PostMapping("/activate")
    public ApiResponse<WorkspaceActivationSummaryDTO> activate(@RequestParam String schoolId,
                                                               @RequestBody(required = false) SchoolActivationRequestDTO request) {
        return ApiResponse.success("Workspace activation checks passed", service.activate(schoolId, request));
    }
}
