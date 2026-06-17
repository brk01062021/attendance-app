package com.school.attendance.controller;

import com.school.attendance.dto.ActivityResponse;
import com.school.attendance.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@CrossOrigin(origins = "*")
public class ActivityFeedController {

    private final ActivityService activityService;

    public ActivityFeedController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> feed(@RequestHeader(value = "X-School-Id", required = false) String schoolId) {
        return ResponseEntity.ok(activityService.feed(schoolId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> detail(@RequestHeader(value = "X-School-Id", required = false) String schoolId,
                                                   @PathVariable Long activityId) {
        return ResponseEntity.ok(activityService.detail(schoolId, activityId));
    }
}
