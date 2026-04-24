package com.school.attendance.repository;

import com.school.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);
    Optional<Attendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate attendanceDate);
    List<Attendance> findByStudentIdAndAttendanceDateBetween(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate
    );
}