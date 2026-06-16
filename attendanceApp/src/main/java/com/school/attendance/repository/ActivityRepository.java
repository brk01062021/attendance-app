package com.school.attendance.repository;

import com.school.attendance.entity.Activity;
import com.school.attendance.entity.ActivityApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Optional<Activity> findByIdAndSchoolIdIgnoreCase(Long id, String schoolId);
    List<Activity> findBySchoolIdIgnoreCaseOrderByActivityDateDescCreatedAtDesc(String schoolId);
    List<Activity> findBySchoolIdIgnoreCaseAndApprovalStatusOrderByActivityDateDescCreatedAtDesc(String schoolId, ActivityApprovalStatus approvalStatus);
}
