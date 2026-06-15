package com.school.attendance.controller;

import com.school.attendance.dto.provisioning.UserProvisioningResponseDTO;
import com.school.attendance.service.provisioning.UserProvisioningService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/user-provisioning", "/user-provisioning"})
@CrossOrigin(origins = "*")
public class UserProvisioningController {
    private final UserProvisioningService userProvisioningService;

    public UserProvisioningController(UserProvisioningService userProvisioningService) {
        this.userProvisioningService = userProvisioningService;
    }

    @PostMapping({"/generate", "/generate-from-workbook"})
    public UserProvisioningResponseDTO generate(@RequestParam(required = false) String schoolId,
                                                @RequestHeader(value = "X-School-Id", required = false) String headerSchoolId) {
        return userProvisioningService.generateFromLatestCommittedWorkbook(schoolId != null && !schoolId.isBlank() ? schoolId : headerSchoolId);
    }

    @GetMapping("/summary")
    public UserProvisioningResponseDTO summary(@RequestParam(required = false) String schoolId,
                                               @RequestHeader(value = "X-School-Id", required = false) String headerSchoolId) {
        return userProvisioningService.summary(schoolId != null && !schoolId.isBlank() ? schoolId : headerSchoolId);
    }
}
