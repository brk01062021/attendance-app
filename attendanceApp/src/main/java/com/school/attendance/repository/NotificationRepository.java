package com.school.attendance.repository;

import com.school.attendance.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndRoleOrderByCreatedAtDesc(
            Long userId,
            String role
    );

    List<Notification> findByUserIdAndRoleAndReadFalseOrderByCreatedAtDesc(
            Long userId,
            String role
    );

    long countByUserIdAndRoleAndReadFalse(Long userId, String role);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
}