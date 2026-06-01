package com.school.attendance.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.dto.WorkspaceActivationAuditDTO;
import com.school.attendance.service.WorkspaceActivationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/activation-notifications", "/api/activation-notifications"})
public class ActivationNotificationController {

    private final WorkspaceActivationService workspaceActivationService;

    public ActivationNotificationController(WorkspaceActivationService workspaceActivationService) {
        this.workspaceActivationService = workspaceActivationService;
    }

    @GetMapping
    public ApiResponse<List<WorkspaceActivationAuditDTO>> notifications(@RequestParam String schoolId) {
        return ApiResponse.success("Activation notification center loaded", workspaceActivationService.getSummary(schoolId).getAuditTrail());
    }
}
