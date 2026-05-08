package com.school.attendance.repository;

import com.school.attendance.entity.SchoolNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolNoticeRepository extends JpaRepository<SchoolNotice, Long> {

    List<SchoolNotice> findByActiveTrueOrderByCreatedAtDesc();

    List<SchoolNotice> findBySchoolIdAndActiveTrueOrderByCreatedAtDesc(Long schoolId);

    List<SchoolNotice> findByTargetRoleAndActiveTrueOrderByCreatedAtDesc(String targetRole);
}