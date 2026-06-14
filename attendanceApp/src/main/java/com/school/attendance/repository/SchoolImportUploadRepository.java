package com.school.attendance.repository;

import com.school.attendance.entity.SchoolImportUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SchoolImportUploadRepository extends JpaRepository<SchoolImportUpload, Long> {
    List<SchoolImportUpload> findTop20BySchoolCodeIgnoreCaseOrderByUploadedAtDesc(String schoolCode);
    List<SchoolImportUpload> findBySchoolCodeIgnoreCaseOrderByUploadedAtDesc(String schoolCode);

    @Query("""
            select u.id as id,
                   u.schoolCode as schoolCode,
                   u.academicYear as academicYear,
                   u.importType as importType,
                   u.fileName as fileName,
                   u.status as status,
                   u.importBatchId as importBatchId,
                   u.totalRows as totalRows,
                   u.totalSheets as totalSheets,
                   u.errorCount as errorCount,
                   u.warningCount as warningCount,
                   u.committed as committed,
                   u.rolledBack as rolledBack,
                   u.stagedRowCount as stagedRowCount,
                   u.lifecycleMessage as lifecycleMessage,
                   u.uploadedByRole as uploadedByRole,
                   u.uploadedAt as uploadedAt,
                   u.committedAt as committedAt,
                   u.rolledBackAt as rolledBackAt
              from SchoolImportUpload u
             where lower(u.schoolCode) = lower(:schoolCode)
             order by u.uploadedAt desc
            """)
    List<SchoolImportUploadSummaryProjection> findUploadSummariesForSchool(@Param("schoolCode") String schoolCode);
    Optional<SchoolImportUpload> findFirstBySchoolCodeIgnoreCaseAndChecksumAndRolledBackFalseOrderByUploadedAtDesc(String schoolCode, String checksum);


    @Query("""
            select u.previewJson
              from SchoolImportUpload u
             where u.id = :uploadId
               and lower(u.schoolCode) = lower(:schoolCode)
            """)
    Optional<String> findPreviewJsonForUpload(@Param("uploadId") Long uploadId, @Param("schoolCode") String schoolCode);
}

