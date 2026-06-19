package com.school.attendance.repository;

import com.school.attendance.entity.TimetableImportFileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimetableImportFileMetadataRepository extends JpaRepository<TimetableImportFileMetadata, Long> {
    Optional<TimetableImportFileMetadata> findTopBySchoolIdOrderByUploadedAtDesc(String schoolId);

    Optional<TimetableImportFileMetadata> findTopByImportBatchIdOrderByUploadedAtDesc(String importBatchId);

    Optional<TimetableImportFileMetadata> findTopBySchoolIdAndStatusOrderByUploadedAtDesc(String schoolId, String status);
}
