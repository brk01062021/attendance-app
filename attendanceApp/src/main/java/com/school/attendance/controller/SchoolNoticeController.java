package com.school.attendance.controller;

import com.school.attendance.dto.SchoolNoticeRequest;
import com.school.attendance.entity.AppUser;
import com.school.attendance.entity.Notification;
import com.school.attendance.entity.SchoolNotice;
import com.school.attendance.entity.Student;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.repository.NotificationRepository;
import com.school.attendance.repository.SchoolNoticeRepository;
import com.school.attendance.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/school-notices")
@CrossOrigin(origins = "*")
public class SchoolNoticeController {

    private final SchoolNoticeRepository schoolNoticeRepository;
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;

    public SchoolNoticeController(
            SchoolNoticeRepository schoolNoticeRepository,
            NotificationRepository notificationRepository,
            StudentRepository studentRepository,
            AppUserRepository appUserRepository
    ) {
        this.schoolNoticeRepository = schoolNoticeRepository;
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping
    public SchoolNotice createSchoolNotice(@RequestBody SchoolNoticeRequest request) {
        SchoolNotice notice = new SchoolNotice();

        notice.setSchoolId(request.getSchoolId());
        notice.setTitle(request.getTitle());
        notice.setMessage(request.getMessage());
        notice.setNoticeType(request.getNoticeType());
        notice.setTargetRole(normalizeTargetRole(request.getTargetRole()));
        notice.setCreatedBy(request.getCreatedBy());
        notice.setActive(true);
        notice.setCreatedAt(LocalDateTime.now());

        SchoolNotice savedNotice = schoolNoticeRepository.save(notice);

        createBroadcastNotifications(savedNotice);

        return savedNotice;
    }

    @GetMapping
    public List<SchoolNotice> getAllActiveNotices() {
        return schoolNoticeRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @GetMapping("/school/{schoolId}")
    public List<SchoolNotice> getNoticesBySchool(@PathVariable Long schoolId) {
        return schoolNoticeRepository.findBySchoolIdAndActiveTrueOrderByCreatedAtDesc(schoolId);
    }

    @PutMapping("/{id}/inactive")
    public SchoolNotice markNoticeInactive(@PathVariable Long id) {
        SchoolNotice notice = schoolNoticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School notice not found with id: " + id));

        notice.setActive(false);
        return schoolNoticeRepository.save(notice);
    }

    private void createBroadcastNotifications(SchoolNotice notice) {
        String targetRole = normalizeTargetRole(notice.getTargetRole());

        List<Notification> notifications = new ArrayList<>();

        if ("ALL".equals(targetRole) || "STUDENT".equals(targetRole)) {
            List<Student> students = studentRepository.findAllByOrderByClassNameAscSectionAscNameAsc();

            for (Student student : students) {
                notifications.add(buildNotification(
                        student.getId(),
                        "STUDENT",
                        notice.getSchoolId(),
                        student.getClassName(),
                        student.getSection(),
                        notice.getTitle(),
                        notice.getMessage(),
                        notice.getNoticeType()
                ));
            }
        }

        if ("ALL".equals(targetRole) || "TEACHER".equals(targetRole)) {
            List<AppUser> teachers = appUserRepository.findByRoleIgnoreCase("TEACHER");

            for (AppUser teacher : teachers) {
                notifications.add(buildNotification(
                        teacher.getId(),
                        "TEACHER",
                        notice.getSchoolId(),
                        null,
                        null,
                        notice.getTitle(),
                        notice.getMessage(),
                        notice.getNoticeType()
                ));
            }
        }

        if ("ALL".equals(targetRole) || "PARENT".equals(targetRole)) {
            List<AppUser> parents = appUserRepository.findByRoleIgnoreCase("PARENT");

            for (AppUser parent : parents) {
                notifications.add(buildNotification(
                        parent.getId(),
                        "PARENT",
                        notice.getSchoolId(),
                        null,
                        null,
                        notice.getTitle(),
                        notice.getMessage(),
                        notice.getNoticeType()
                ));
            }
        }

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
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

    private String normalizeTargetRole(String targetRole) {
        if (targetRole == null || targetRole.trim().isEmpty()) {
            return "ALL";
        }

        return targetRole.trim().toUpperCase();
    }
}