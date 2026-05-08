package com.school.attendance.controller;

import com.school.attendance.dto.BulkNotificationRequest;
import com.school.attendance.entity.AppUser;
import com.school.attendance.entity.Notification;
import com.school.attendance.entity.Student;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.repository.NotificationRepository;
import com.school.attendance.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;

    public NotificationController(
            NotificationRepository notificationRepository,
            StudentRepository studentRepository,
            AppUserRepository appUserRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @PostMapping("/bulk")
    public List<Notification> createBulkNotifications(@RequestBody BulkNotificationRequest request) {
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            throw new RuntimeException("User ids are required");
        }

        List<Notification> notifications = new ArrayList<>();

        for (Long userId : request.getUserIds()) {
            notifications.add(buildNotification(
                    userId,
                    request.getRole(),
                    request.getSchoolId(),
                    request.getClassName(),
                    request.getSection(),
                    request.getTitle(),
                    request.getMessage(),
                    request.getType()
            ));
        }

        return notificationRepository.saveAll(notifications);
    }

    @PostMapping("/broadcast/school")
    public List<Notification> broadcastToSchool(@RequestBody BulkNotificationRequest request) {
        List<Student> students = studentRepository.findAllByOrderByClassNameAscSectionAscNameAsc();
        return createStudentNotifications(students, request);
    }

    @PostMapping("/broadcast/class")
    public List<Notification> broadcastToClass(@RequestBody BulkNotificationRequest request) {
        List<Student> students = studentRepository.findByClassNameOrderBySectionAscNameAsc(
                request.getClassName()
        );

        return createStudentNotifications(students, request);
    }

    @PostMapping("/broadcast/section")
    public List<Notification> broadcastToSection(@RequestBody BulkNotificationRequest request) {
        List<Student> students = studentRepository.findByClassNameAndSectionOrderByNameAsc(
                request.getClassName(),
                request.getSection()
        );

        return createStudentNotifications(students, request);
    }

    @PostMapping("/broadcast/role")
    public List<Notification> broadcastToRole(@RequestBody BulkNotificationRequest request) {
        List<AppUser> users = appUserRepository.findByRoleIgnoreCase(request.getRole());

        List<Notification> notifications = new ArrayList<>();

        for (AppUser user : users) {
            notifications.add(buildNotification(
                    user.getId(),
                    user.getRole(),
                    request.getSchoolId(),
                    request.getClassName(),
                    request.getSection(),
                    request.getTitle(),
                    request.getMessage(),
                    request.getType()
            ));
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

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(
            @RequestParam Long userId,
            @RequestParam String role
    ) {
        return notificationRepository.findByUserIdAndRoleAndReadFalseOrderByCreatedAtDesc(userId, role);
    }

    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @PutMapping("/{id}/unread")
    public Notification markAsUnread(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        notification.setRead(false);
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

    private List<Notification> createStudentNotifications(
            List<Student> students,
            BulkNotificationRequest request
    ) {
        List<Notification> notifications = new ArrayList<>();

        for (Student student : students) {
            notifications.add(buildNotification(
                    student.getId(),
                    "STUDENT",
                    request.getSchoolId(),
                    student.getClassName(),
                    student.getSection(),
                    request.getTitle(),
                    request.getMessage(),
                    request.getType()
            ));
        }

        return notificationRepository.saveAll(notifications);
    }

    private Notification buildNotification(
            Long userId,
            String role,
            Long schoolId,
            String className,
            String section,
            String title,
            String message,
            String type
    ) {
        Notification notification = new Notification();

        notification.setUserId(userId);
        notification.setRole(role);
        notification.setSchoolId(schoolId);
        notification.setClassName(className);
        notification.setSection(section);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notification;
    }
}