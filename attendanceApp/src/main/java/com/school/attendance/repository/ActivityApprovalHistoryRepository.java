package com.school.attendance.repository;

import com.school.attendance.entity.ActivityApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityApprovalHistoryRepository extends JpaRepository<ActivityApprovalHistory, Long> {
    List<ActivityApprovalHistory> findByActivityIdAndSchoolIdIgnoreCaseOrderByActionTimeDesc(Long activityId, String schoolId);
}
