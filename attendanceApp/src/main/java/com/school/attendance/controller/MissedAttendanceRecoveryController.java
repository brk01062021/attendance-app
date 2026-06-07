package com.school.attendance.controller;

import com.school.attendance.dto.MissedAttendanceRecoveryResponseDTO;
import com.school.attendance.dto.MissedAttendanceRecoveryStatusDTO;
import com.school.attendance.service.MissedAttendanceRecoveryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/attendance/recovery")
@CrossOrigin(origins = "*")
public class MissedAttendanceRecoveryController {
    private final MissedAttendanceRecoveryService service;
    public MissedAttendanceRecoveryController(MissedAttendanceRecoveryService service) { this.service = service; }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vidyasetu-missed-attendance-template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(service.template());
    }

    @PostMapping("/validate")
    public MissedAttendanceRecoveryResponseDTO validate(@RequestPart("file") MultipartFile file, @RequestHeader(value="X-School-Id", required=false) String headerSchoolId, @RequestParam(required=false) String schoolId, @RequestParam(required=false) String uploadedBy) {
        return service.validate(file, schoolId != null && !schoolId.isBlank() ? schoolId : headerSchoolId, uploadedBy);
    }

    @PostMapping("/submit/{batchId}")
    public MissedAttendanceRecoveryResponseDTO submit(@PathVariable String batchId, @RequestHeader(value="X-School-Id", required=false) String headerSchoolId, @RequestParam(required=false) String schoolId, @RequestParam(required=false) String submittedBy) {
        return service.submit(batchId, schoolId != null && !schoolId.isBlank() ? schoolId : headerSchoolId, submittedBy);
    }

    @GetMapping("/status")
    public MissedAttendanceRecoveryStatusDTO status(@RequestHeader(value="X-School-Id", required=false) String headerSchoolId, @RequestParam(required=false) String schoolId) {
        return service.status(schoolId != null && !schoolId.isBlank() ? schoolId : headerSchoolId);
    }
}
