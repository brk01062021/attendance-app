package com.school.attendance.repository;

import com.school.attendance.entity.FeeReminderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeeReminderHistoryRepository extends JpaRepository<FeeReminderHistory, Long> {
    List<FeeReminderHistory> findTop100BySchoolIdIgnoreCaseOrderBySentAtDesc(String schoolId);
    List<FeeReminderHistory> findTop50BySchoolIdIgnoreCaseAndParentUserIdOrderBySentAtDesc(String schoolId, Long parentUserId);
    void deleteBySchoolIdIgnoreCaseAndUploadIdIn(String schoolId, List<Long> uploadIds);
}
