package com.school.attendance.controller;

import com.school.attendance.dto.GroupedReplacementOptionsDTO;
import com.school.attendance.dto.ReplacementTeacherDTO;
import com.school.attendance.entity.TeacherSchedule;
import com.school.attendance.entity.TeacherScheduleStatus;
import com.school.attendance.repository.TeacherScheduleRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher-schedules")
public class TeacherScheduleController {

    private final TeacherScheduleRepository teacherScheduleRepository;

    public TeacherScheduleController(TeacherScheduleRepository teacherScheduleRepository) {
        this.teacherScheduleRepository = teacherScheduleRepository;
    }

    @GetMapping
    public List<TeacherSchedule> getSchedulesByDate(@RequestParam String date) {
        LocalDate scheduleDate = LocalDate.parse(date);

        return teacherScheduleRepository
                .findByScheduleDateOrderByStartTimeAscTeacherNameAsc(scheduleDate);
    }

    @GetMapping("/teacher")
    public List<TeacherSchedule> getSchedulesByTeacherAndDate(
            @RequestParam Long teacherId,
            @RequestParam String date
    ) {
        LocalDate scheduleDate = LocalDate.parse(date);

        return teacherScheduleRepository
                .findByTeacherIdAndScheduleDateOrderByStartTimeAsc(
                        teacherId,
                        scheduleDate
                );
    }

    /*
     * NEW:
     * teacher dropdown list for selected date
     */
    @GetMapping("/teachers")
    public List<TeacherSchedule> getTeachersByDate(@RequestParam String date) {
        LocalDate scheduleDate = LocalDate.parse(date);

        return teacherScheduleRepository
                .findByScheduleDateOrderByTeacherNameAscStartTimeAsc(scheduleDate);
    }

    /*
     * NEW:
     * mark full-day leave
     */
    @PutMapping("/teacher/{teacherId}/full-day-leave")
    public List<TeacherSchedule> markTeacherFullDayLeave(
            @PathVariable Long teacherId,
            @RequestParam String date,
            @RequestParam String status
    ) {
        LocalDate scheduleDate = LocalDate.parse(date);

        TeacherScheduleStatus leaveStatus =
                TeacherScheduleStatus.valueOf(status.toUpperCase());

        List<TeacherSchedule> schedules =
                teacherScheduleRepository.findByTeacherIdAndScheduleDate(
                        teacherId,
                        scheduleDate
                );

        for (TeacherSchedule schedule : schedules) {
            schedule.setStatus(leaveStatus);
            schedule.setReplacementTeacherId(null);
            schedule.setReplacementTeacherName("No replacement assigned");
        }

        return teacherScheduleRepository.saveAll(schedules);
    }

    @PostMapping
    public TeacherSchedule createSchedule(@RequestBody TeacherSchedule schedule) {
        if (schedule.getStatus() == null) {
            schedule.setStatus(TeacherScheduleStatus.AVAILABLE);
        }

        schedule.setReplacementClass(false);

        return teacherScheduleRepository.save(schedule);
    }

    @PutMapping("/{id}/status")
    public TeacherSchedule updateScheduleStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Long replacementTeacherId,
            @RequestParam(required = false) String replacementTeacherName
    ) {
        TeacherSchedule schedule = teacherScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Teacher schedule not found with id: " + id
                ));

        TeacherScheduleStatus newStatus =
                TeacherScheduleStatus.valueOf(status.toUpperCase());

        schedule.setStatus(newStatus);

        schedule.setReplacementTeacherId(null);
        schedule.setReplacementTeacherName(null);

        if (replacementTeacherId != null
                && replacementTeacherName != null
                && !replacementTeacherName.isBlank()) {

            schedule.setReplacementTeacherId(replacementTeacherId);
            schedule.setReplacementTeacherName(replacementTeacherName);

        } else if (newStatus == TeacherScheduleStatus.PLANNED_LEAVE
                || newStatus == TeacherScheduleStatus.UNPLANNED_LEAVE) {

            schedule.setReplacementTeacherName("No replacement assigned");
        }

        return teacherScheduleRepository.save(schedule);
    }

    @GetMapping("/available-replacements")
    public GroupedReplacementOptionsDTO getAvailableReplacementTeachers(
            @RequestParam Long scheduleId
    ) {
        TeacherSchedule leaveSchedule = teacherScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException(
                        "Teacher schedule not found with id: " + scheduleId
                ));

        List<TeacherSchedule> sameDaySchedules =
                teacherScheduleRepository.findByScheduleDate(
                        leaveSchedule.getScheduleDate()
                );

        Map<Long, TeacherSchedule> allTeachersMap = new LinkedHashMap<>();

        for (TeacherSchedule schedule : sameDaySchedules) {
            if (!schedule.getTeacherId().equals(leaveSchedule.getTeacherId())) {
                allTeachersMap.putIfAbsent(schedule.getTeacherId(), schedule);
            }
        }

        List<ReplacementTeacherDTO> availableTeachers = new ArrayList<>();

        for (Map.Entry<Long, TeacherSchedule> teacherEntry : allTeachersMap.entrySet()) {
            Long teacherId = teacherEntry.getKey();
            TeacherSchedule teacherSchedule = teacherEntry.getValue();

            boolean hasOverlap = false;

            for (TeacherSchedule schedule : sameDaySchedules) {
                if (!schedule.getTeacherId().equals(teacherId)) {
                    continue;
                }

                boolean overlaps =
                        schedule.getStartTime().isBefore(leaveSchedule.getEndTime())
                                && schedule.getEndTime().isAfter(
                                leaveSchedule.getStartTime()
                        );

                if (overlaps) {
                    hasOverlap = true;
                    break;
                }
            }

            if (!hasOverlap && scheduleAvailableForReplacement(teacherSchedule)) {
                availableTeachers.add(
                        new ReplacementTeacherDTO(
                                teacherId,
                                teacherSchedule.getTeacherName(),
                                teacherSchedule.getClassName(),
                                teacherSchedule.getSection(),
                                teacherSchedule.getSubjectName(),
                                ""
                        )
                );
            }
        }

        List<ReplacementTeacherDTO> bestMatch = new ArrayList<>();
        List<ReplacementTeacherDTO> sameClass = new ArrayList<>();
        List<ReplacementTeacherDTO> others = new ArrayList<>();

        for (ReplacementTeacherDTO teacher : availableTeachers) {
            int dailyWorkload = 0;
            LocalTime previousEndTime = null;
            LocalTime nextStartTime = null;

            for (TeacherSchedule schedule : sameDaySchedules) {
                if (!schedule.getTeacherId().equals(teacher.getTeacherId())) {
                    continue;
                }

                dailyWorkload++;

                if (schedule.getEndTime().isBefore(leaveSchedule.getStartTime())
                        || schedule.getEndTime().equals(leaveSchedule.getStartTime())) {

                    if (previousEndTime == null || schedule.getEndTime().isAfter(previousEndTime)) {
                        previousEndTime = schedule.getEndTime();
                    }
                }

                if (schedule.getStartTime().isAfter(leaveSchedule.getEndTime())
                        || schedule.getStartTime().equals(leaveSchedule.getEndTime())) {

                    if (nextStartTime == null || schedule.getStartTime().isBefore(nextStartTime)) {
                        nextStartTime = schedule.getStartTime();
                    }
                }
            }

            long beforeGap = previousEndTime == null
                    ? -1
                    : Duration.between(previousEndTime, leaveSchedule.getStartTime()).toMinutes();

            long afterGap = nextStartTime == null
                    ? -1
                    : Duration.between(leaveSchedule.getEndTime(), nextStartTime).toMinutes();

            teacher.setDailyWorkload(dailyWorkload);

            if (beforeGap < 0) {
                teacher.setLastClassEnded("First class today");
            } else if (beforeGap >= 60) {
                teacher.setLastClassEnded((beforeGap / 60) + " hr ago");
            } else {
                teacher.setLastClassEnded(beforeGap + " mins ago");
            }

            if (afterGap < 0) {
                teacher.setNextClass("No more classes today");
            } else if (afterGap >= 60) {
                teacher.setNextClass("after " + (afterGap / 60) + " hrs");
            } else {
                teacher.setNextClass("after " + afterGap + " mins");
            }

            boolean sameClassName =
                    leaveSchedule.getClassName() != null
                            && teacher.getClassName() != null
                            && leaveSchedule.getClassName().equalsIgnoreCase(
                            teacher.getClassName()
                    );

            boolean sameSection =
                    leaveSchedule.getSection() != null
                            && teacher.getSection() != null
                            && leaveSchedule.getSection().equalsIgnoreCase(
                            teacher.getSection()
                    );

            if (sameClassName && sameSection) {
                teacher.setMatchType("BEST_MATCH");
                bestMatch.add(teacher);
            } else if (sameClassName) {
                teacher.setMatchType("SAME_CLASS");
                sameClass.add(teacher);
            } else {
                teacher.setMatchType("OTHER");
                others.add(teacher);
            }
        }

        Comparator<ReplacementTeacherDTO> replacementSorter =
                Comparator.comparingInt(ReplacementTeacherDTO::getDailyWorkload);

        bestMatch.sort(replacementSorter);
        sameClass.sort(replacementSorter);
        others.sort(replacementSorter);

        return new GroupedReplacementOptionsDTO(
                bestMatch,
                sameClass,
                others
        );
    }

    private boolean scheduleAvailableForReplacement(TeacherSchedule schedule) {
        return schedule.getStatus() != TeacherScheduleStatus.PLANNED_LEAVE
                && schedule.getStatus() != TeacherScheduleStatus.UNPLANNED_LEAVE;
    }

    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable Long id) {
        teacherScheduleRepository.deleteById(id);
    }
}