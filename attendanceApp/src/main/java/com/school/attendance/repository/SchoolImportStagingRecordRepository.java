package com.school.attendance.repository;

import com.school.attendance.entity.SchoolImportStagingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolImportStagingRecordRepository extends JpaRepository<SchoolImportStagingRecord, Long> {
    long countByUploadIdAndStatus(Long uploadId, String status);
    List<SchoolImportStagingRecord> findByUploadId(Long uploadId);
    List<SchoolImportStagingRecord> findBySchoolCodeIgnoreCaseAndImportBatchIdIgnoreCaseAndStatusOrderByWorkbookRowNumberAsc(String schoolCode, String importBatchId, String status);
    List<SchoolImportStagingRecord> findBySchoolCodeIgnoreCaseAndStatusOrderByStagedAtDescWorkbookRowNumberAsc(String schoolCode, String status);
    void deleteByUploadId(Long uploadId);
}
