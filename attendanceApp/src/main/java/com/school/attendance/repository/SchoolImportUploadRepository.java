package com.school.attendance.repository;

import com.school.attendance.entity.SchoolImportUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolImportUploadRepository extends JpaRepository<SchoolImportUpload, Long> {
    List<SchoolImportUpload> findTop20BySchoolCodeIgnoreCaseOrderByUploadedAtDesc(String schoolCode);
    Optional<SchoolImportUpload> findFirstBySchoolCodeIgnoreCaseAndChecksumAndRolledBackFalseOrderByUploadedAtDesc(String schoolCode, String checksum);
}
