package com.school.attendance.repository;

import com.school.attendance.entity.Attendance;
import com.school.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT a FROM Attendance a
            WHERE (:teacherId IS NULL OR a.teacherId = :teacherId)
              AND (:subjectName IS NULL OR LOWER(a.subjectName) = LOWER(:subjectName))
              AND (:className IS NULL OR LOWER(a.className) = LOWER(:className))
              AND (:section IS NULL OR LOWER(a.section) = LOWER(:section))
              AND (:attendanceDate IS NULL OR a.attendanceDate = :attendanceDate)
            ORDER BY a.attendanceDate DESC, a.className ASC, a.section ASC, a.subjectName ASC
            """)
    List<Attendance> findFilteredAttendance(
            @Param("teacherId") Long teacherId,
            @Param("subjectName") String subjectName,
            @Param("className") String className,
            @Param("section") String section,
            @Param("attendanceDate") LocalDate attendanceDate
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