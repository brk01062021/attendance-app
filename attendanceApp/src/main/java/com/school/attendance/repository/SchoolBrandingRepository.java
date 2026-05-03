package com.school.attendance.repository;

import com.school.attendance.entity.SchoolBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolBrandingRepository extends JpaRepository<SchoolBranding, Long> {
}