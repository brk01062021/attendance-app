package com.school.attendance.controller;

import com.school.attendance.dto.BulkNotificationRequest;
import com.school.attendance.entity.Notification;
import com.school.attendance.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @PostMapping("/bulk")
    public List<Notification> createBulkNotifications(@RequestBody BulkNotificationRequest request) {
        List<Notification> notifications = new ArrayList<>();

        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            throw new RuntimeException("User ids are required");
        }

        for (Long userId : request.getUserIds()) {
            Notification notification = new Notification();

            notification.setUserId(userId);
            notification.setRole(request.getRole());
            notification.setSchoolId(request.getSchoolId());
            notification.setClassName(request.getClassName());
            notification.setSection(request.getSection());

            notification.setTitle(request.getTitle());
            notification.setMessage(request.getMessage());
            notification.setType(request.getType());
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());

            notifications.add(notification);
        }

        return notificationRepository.saveAll(notifications);
    }

    @GetMapping
    public List<Notification> getNotifications(
            @RequestParam Long userId,
            @RequestParam String role
    ) {
        return notificationRepository.findByUserIdAndRoleOrderByCreatedAtDesc(userId, role);
    }

    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @PutMapping("/mark-all-read")
    public List<Notification> markAllAsRead(
            @RequestParam Long userId,
            @RequestParam String role
    ) {
        List<Notification> notifications =
                notificationRepository.findByUserIdAndRoleOrderByCreatedAtDesc(userId, role);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        return notificationRepository.saveAll(notifications);
    }
}