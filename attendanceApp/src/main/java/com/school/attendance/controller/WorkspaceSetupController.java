package com.school.attendance.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.dto.WorkspaceChecklistDTO;
import com.school.attendance.dto.WorkspaceStepUpdateRequest;
import com.school.attendance.service.WorkspaceSetupService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/workspace-setup", "/api/workspace-setup"})
public class WorkspaceSetupController {
    private final WorkspaceSetupService service;

    public WorkspaceSetupController(WorkspaceSetupService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public ApiResponse<WorkspaceChecklistDTO> status(@RequestParam String schoolId) {
        return ApiResponse.success("Workspace setup status loaded", service.getOrCreate(schoolId));
    }

    @PostMapping("/{stepKey}")
    public ApiResponse<WorkspaceChecklistDTO> updateStep(@RequestParam String schoolId,
                                                         @PathVariable String stepKey,
                                                         @RequestBody WorkspaceStepUpdateRequest request) {
        return ApiResponse.success("Workspace setup step saved", service.updateStep(schoolId, stepKey, request));
    }

    @GetMapping("/import-lock")
    public ApiResponse<WorkspaceChecklistDTO> importLock(@RequestParam String schoolId) {
        WorkspaceChecklistDTO checklist = service.getOrCreate(schoolId);
        return ApiResponse.success(checklist.isImportLocked() ? "Import School Data is locked" : "Import School Data is unlocked", checklist);
    }
}
