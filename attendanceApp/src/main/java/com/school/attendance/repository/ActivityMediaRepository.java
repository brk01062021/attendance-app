package com.school.attendance.repository;

import com.school.attendance.entity.ActivityMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityMediaRepository extends JpaRepository<ActivityMedia, Long> {
    List<ActivityMedia> findByActivityIdAndSchoolIdIgnoreCaseOrderByUploadedAtAsc(Long activityId, String schoolId);
}
