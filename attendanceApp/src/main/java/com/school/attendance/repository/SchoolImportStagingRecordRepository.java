package com.school.attendance.repository;

import com.school.attendance.entity.SchoolImportStagingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolImportStagingRecordRepository extends JpaRepository<SchoolImportStagingRecord, Long> {
    long countByUploadIdAndStatus(Long uploadId, String status);
    List<SchoolImportStagingRecord> findByUploadId(Long uploadId);
    void deleteByUploadId(Long uploadId);
}
