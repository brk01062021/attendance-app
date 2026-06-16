package com.school.attendance.repository;

import com.school.attendance.entity.ActivityClassVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityClassVisibilityRepository extends JpaRepository<ActivityClassVisibility, Long> {
    List<ActivityClassVisibility> findByActivityIdAndSchoolIdIgnoreCase(Long activityId, String schoolId);
    void deleteByActivityIdAndSchoolIdIgnoreCase(Long activityId, String schoolId);
}
