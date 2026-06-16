package com.school.attendance.controller;

import com.school.attendance.dto.ActivityApprovalRequest;
import com.school.attendance.dto.ActivityResponse;
import com.school.attendance.dto.CreateActivityRequest;
import com.school.attendance.dto.UpdateActivityRequest;
import com.school.attendance.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> create(@RequestHeader(value = "X-School-Id", required = false) String schoolId,
                                                   @RequestBody CreateActivityRequest request) {
        return ResponseEntity.ok(activityService.create(schoolId, request, false));
    }

    @PostMapping("/teacher")
    public ResponseEntity<ActivityResponse> createTeacherDraft(@RequestHeader(value = "X-School-Id", required = false) String schoolId,
                                                               @RequestBody CreateActivityRequest request) {
        return ResponseEntity.ok(activityService.create(schoolId, request, true));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> update(@RequestHeader(value = "X-School-Id", required = false) String schoolId,
                                                   @PathVariable Long id,
                                                   @RequestBody UpdateActivityRequest request) {
        return ResponseEntity.ok(activityService.update(schoolId, id, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ActivityResponse> submit(@RequestHeader(value = "X-School-Id", required = false) String schoolId,
                                                   @PathVariable Long id,
                                                   @RequestBody(required = false) ActivityApprovalRequest request) {
        return ResponseEntity.ok(activityService.submit(schoolId, id, request));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ActivityResponse> approve(@RequestHeader(value = "X-School-Id", required = false) String schoolId,
                                                    @PathVariable Long id,
                                                    @RequestBody(required = false) ActivityApprovalRequest request) {
        return ResponseEntity.ok(activityService.approve(schoolId, id, request));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ActivityResponse> reject(@RequestHeader(value = "X-School-Id", required = false) String schoolId,
                                                   @PathVariable Long id,
                                                   @RequestBody(required = false) ActivityApprovalRequest request) {
        return ResponseEntity.ok(activityService.reject(schoolId, id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ActivityResponse> publish(@RequestHeader(value = "X-School-Id", required = false) String schoolId,
                                                    @PathVariable Long id,
                                                    @RequestBody(required = false) ActivityApprovalRequest request) {
        return ResponseEntity.ok(activityService.publish(schoolId, id, request));
    }


    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getActivities(@RequestHeader(value = "X-School-Id", required = false) String headerSchoolId,
                                                                @RequestParam(value = "schoolId", required = false) String querySchoolId) {
        String schoolId = querySchoolId != null && !querySchoolId.isBlank() ? querySchoolId : headerSchoolId;
        return ResponseEntity.ok(activityService.getActivities(schoolId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ActivityResponse>> pending(@RequestHeader(value = "X-School-Id", required = false) String schoolId) {
        return ResponseEntity.ok(activityService.pending(schoolId));
    }
}
