package com.school.attendance.repository;

import com.school.attendance.entity.ActivityStudentVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityStudentVisibilityRepository extends JpaRepository<ActivityStudentVisibility, Long> {
    List<ActivityStudentVisibility> findByActivityIdAndSchoolIdIgnoreCase(Long activityId, String schoolId);
    void deleteByActivityIdAndSchoolIdIgnoreCase(Long activityId, String schoolId);
}
