package com.school.attendance.controller;

import com.school.attendance.dto.provisioning.UserProvisioningCredentialDTO;
import com.school.attendance.dto.provisioning.UserProvisioningResponseDTO;
import com.school.attendance.service.provisioning.UserProvisioningService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

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

    @GetMapping("/credentials/{role}")
    public List<UserProvisioningCredentialDTO> credentials(@PathVariable String role,
                                                           @RequestParam(required = false) String schoolId,
                                                           @RequestHeader(value = "X-School-Id", required = false) String headerSchoolId) {
        return userProvisioningService.downloadableCredentials(schoolId != null && !schoolId.isBlank() ? schoolId : headerSchoolId, role);
    }

    @GetMapping(value = "/credentials/{role}/download", produces = "text/csv")
    public ResponseEntity<byte[]> downloadCredentials(@PathVariable String role,
                                                      @RequestParam(required = false) String schoolId,
                                                      @RequestHeader(value = "X-School-Id", required = false) String headerSchoolId) {
        String safeRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        List<UserProvisioningCredentialDTO> credentials = userProvisioningService.downloadableCredentials(schoolId != null && !schoolId.isBlank() ? schoolId : headerSchoolId, safeRole);
        StringBuilder csv = new StringBuilder("Role,Name,Username,Temporary Password,Linked Reference,First Login Instruction\n");
        for (UserProvisioningCredentialDTO credential : credentials) {
            csv.append(csv(credential.getRole())).append(',')
                    .append(csv(credential.getDisplayName())).append(',')
                    .append(csv(credential.getUsername())).append(',')
                    .append(csv(credential.getTemporaryPassword())).append(',')
                    .append(csv(credential.getLinkedReference())).append(',')
                    .append(csv("Login with this temporary password and create a new password on first login"))
                    .append('\n');
        }
        String filename = safeRole.toLowerCase(Locale.ROOT) + "-credentials.csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}

