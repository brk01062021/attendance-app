package com.school.attendance.repository;

import com.school.attendance.entity.TeacherSchedule;
import com.school.attendance.entity.TeacherScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TeacherScheduleRepository extends JpaRepository<TeacherSchedule, Long> {

    List<TeacherSchedule> findBySchoolIdIgnoreCaseAndActiveTimetableTrueOrderByScheduleDateAscStartTimeAscTeacherNameAsc(String schoolId);

    Integer countBySchoolIdIgnoreCaseAndActiveTimetableTrue(String schoolId);

    void deleteBySchoolIdIgnoreCaseAndActiveTimetableTrue(String schoolId);

    List<TeacherSchedule> findBySchoolIdIgnoreCaseAndImportBatchIdOrderByScheduleDateAscStartTimeAscTeacherNameAsc(String schoolId, String importBatchId);

    List<TeacherSchedule> findByScheduleDateOrderByStartTimeAscTeacherNameAsc(LocalDate scheduleDate);

    List<TeacherSchedule> findByTeacherIdAndScheduleDateOrderByStartTimeAsc(
            Long teacherId,
            LocalDate scheduleDate
    );

    List<TeacherSchedule> findByScheduleDate(LocalDate scheduleDate);

    List<TeacherSchedule> findByScheduleDateOrderByTeacherNameAscStartTimeAsc(LocalDate scheduleDate);

    List<TeacherSchedule> findByTeacherIdAndScheduleDate(
            Long teacherId,
            LocalDate scheduleDate
    );

    List<TeacherSchedule> findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(
            LocalDate fromDate,
            LocalDate toDate
    );

    List<TeacherSchedule> findByScheduleDateAndStartTimeAndEndTime(
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime
    );

    List<TeacherSchedule> findByTeacherIdAndScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(
            Long teacherId,
            LocalDate fromDate,
            LocalDate toDate
    );

    List<TeacherSchedule> findByScheduleDateAndReplacementTeacherIdIsNull(
            LocalDate scheduleDate
    );

    List<TeacherSchedule> findByScheduleDateAndTeacherIdAndReplacementTeacherIdIsNull(
            LocalDate scheduleDate,
            Long teacherId
    );

    List<TeacherSchedule> findByScheduleDateAndStartTimeAndEndTimeAndTeacherIdNot(
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime,
            Long teacherId
    );

    // Teacher Insight Report additions.
    List<TeacherSchedule> findByTeacherIdOrderByScheduleDateDescStartTimeAsc(Long teacherId);

    List<TeacherSchedule> findByTeacherIdAndScheduleDateBetweenOrderByScheduleDateDescStartTimeAsc(
            Long teacherId,
            LocalDate fromDate,
            LocalDate toDate
    );

    List<TeacherSchedule> findByReplacementTeacherIdOrderByScheduleDateDescStartTimeAsc(Long replacementTeacherId);

    List<TeacherSchedule> findByReplacementTeacherIdAndScheduleDateBetweenOrderByScheduleDateDescStartTimeAsc(
            Long replacementTeacherId,
            LocalDate fromDate,
            LocalDate toDate
    );

    Integer countByTeacherIdAndStatus(Long teacherId, TeacherScheduleStatus status);

    Integer countByReplacementTeacherId(Long replacementTeacherId);
}
