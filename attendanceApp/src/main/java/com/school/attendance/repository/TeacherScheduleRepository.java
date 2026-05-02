package com.school.attendance.repository;

import com.school.attendance.entity.TeacherSchedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherScheduleRepository extends JpaRepository<TeacherSchedule, Long> {

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
}