package com.school.attendance.repository;

import com.school.attendance.entity.ActivityMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityMediaRepository extends JpaRepository<ActivityMedia, Long> {
    List<ActivityMedia> findByActivityIdAndSchoolIdIgnoreCaseOrderByDisplayOrderAscUploadedAtAsc(Long activityId, String schoolId);
    List<ActivityMedia> findByActivityIdAndSchoolIdIgnoreCaseOrderByUploadedAtAsc(Long activityId, String schoolId);
    Optional<ActivityMedia> findByIdAndActivityIdAndSchoolIdIgnoreCase(Long id, Long activityId, String schoolId);
}
