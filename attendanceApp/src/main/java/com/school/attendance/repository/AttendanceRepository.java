package com.school.attendance.repository;

import com.school.attendance.entity.Attendance;
import com.school.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);

    long countByAttendanceDate(LocalDate attendanceDate);

    long countByAttendanceDateAndStatus(LocalDate attendanceDate, AttendanceStatus status);

    Optional<Attendance> findByStudentIdAndAttendanceDateAndTeacherIdAndSubjectNameAndClassNameAndSection(
            Long studentId,
            LocalDate attendanceDate,
            Long teacherId,
            String subjectName,
            String className,
            String section
    );

    List<Attendance> findByStudentIdAndAttendanceDateBetween(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Attendance> findByTeacherIdAndSubjectNameAndClassNameAndSectionAndAttendanceDate(
            Long teacherId,
            String subjectName,
            String className,
            String section,
            LocalDate attendanceDate
    );

    List<Attendance> findByTeacherIdAndAttendanceDate(
            Long teacherId,
            LocalDate attendanceDate
    );

    List<Attendance> findByAttendanceDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );
}