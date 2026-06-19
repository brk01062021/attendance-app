package com.school.attendance.repository;

import com.school.attendance.entity.FeeReminderUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeeReminderUploadRepository extends JpaRepository<FeeReminderUpload, Long> {
    List<FeeReminderUpload> findTop20BySchoolIdIgnoreCaseOrderByCreatedAtDesc(String schoolId);
    List<FeeReminderUpload> findBySchoolIdIgnoreCaseAndIdIn(String schoolId, List<Long> ids);
}
