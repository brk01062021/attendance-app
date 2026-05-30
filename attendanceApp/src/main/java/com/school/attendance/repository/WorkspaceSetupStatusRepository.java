package com.school.attendance.repository;

import com.school.attendance.entity.WorkspaceSetupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceSetupStatusRepository extends JpaRepository<WorkspaceSetupStatus, Long> {
    Optional<WorkspaceSetupStatus> findBySchoolIdIgnoreCase(String schoolId);
}
