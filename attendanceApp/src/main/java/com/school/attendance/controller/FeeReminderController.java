package com.school.attendance.controller;

import com.school.attendance.dto.FeeReminderDtos;
import com.school.attendance.service.FeeReminderService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/fee-reminders")
@CrossOrigin(origins = "*")
public class FeeReminderController {
    private final FeeReminderService service;
    public FeeReminderController(FeeReminderService service) { this.service = service; }

    @PostMapping("/upload")
    public FeeReminderDtos.Preview upload(@RequestParam String schoolId, @RequestParam MultipartFile file, @RequestParam(required = false) String uploadedBy) {
        return service.upload(schoolId, file, uploadedBy);
    }
    @GetMapping("/uploads")
    public List<FeeReminderDtos.Summary> uploads(@RequestParam String schoolId) { return service.uploads(schoolId); }
    @GetMapping("/{uploadId}/preview")
    public FeeReminderDtos.Preview preview(@PathVariable Long uploadId, @RequestParam String schoolId) { return service.preview(uploadId, schoolId); }
    @GetMapping("/{uploadId}/summary")
    public FeeReminderDtos.Summary summary(@PathVariable Long uploadId, @RequestParam String schoolId) { return service.summary(uploadId, schoolId); }
    @PostMapping("/{uploadId}/send")
    public FeeReminderDtos.SendResult send(@PathVariable Long uploadId, @RequestParam String schoolId, @RequestParam(required = false) String sentBy) { return service.send(uploadId, schoolId, sentBy); }
    @GetMapping("/history")
    public List<FeeReminderDtos.History> history(@RequestParam String schoolId) { return service.history(schoolId); }
    @DeleteMapping("/uploads")
    public Map<String, Object> deleteUploads(@RequestParam String schoolId, @RequestBody Map<String, List<Long>> body) { return service.deleteUploads(schoolId, body == null ? List.of() : body.get("uploadIds")); }
    @GetMapping("/parent-history")
    public List<FeeReminderDtos.History> parentHistory(@RequestParam String schoolId, @RequestParam Long parentUserId) { return service.parentHistory(schoolId, parentUserId); }
}
