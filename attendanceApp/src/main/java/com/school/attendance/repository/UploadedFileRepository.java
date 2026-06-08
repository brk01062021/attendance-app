package com.school.attendance.repository;

import com.school.attendance.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findBySchoolIdOrderByCreatedAtDesc(String schoolId);

    List<UploadedFile> findBySchoolIdAndModuleOrderByCreatedAtDesc(String schoolId, String module);
}