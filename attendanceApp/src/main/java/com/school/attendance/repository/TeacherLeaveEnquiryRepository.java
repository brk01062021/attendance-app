package com.school.attendance.repository;

import com.school.attendance.entity.TeacherLeaveEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TeacherLeaveEnquiryRepository extends JpaRepository<TeacherLeaveEnquiry, Long> {
    List<TeacherLeaveEnquiry> findByStatusOrderByRequestedAtDesc(String status);
    List<TeacherLeaveEnquiry> findByFromDateLessThanEqualAndToDateGreaterThanEqualOrderByRequestedAtDesc(LocalDate toDate, LocalDate fromDate);
    List<TeacherLeaveEnquiry> findByTeacherIdOrderByRequestedAtDesc(Long teacherId);
}
