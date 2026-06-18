package com.school.attendance.repository;

import com.school.attendance.entity.FeeReminderRow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeeReminderRowRepository extends JpaRepository<FeeReminderRow, Long> {
    List<FeeReminderRow> findByUploadIdOrderByRowNumberAsc(Long uploadId);
    List<FeeReminderRow> findByUploadIdAndStatusOrderByRowNumberAsc(Long uploadId, String status);
}
