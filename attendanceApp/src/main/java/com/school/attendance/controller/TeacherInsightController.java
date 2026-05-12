package com.school.attendance.controller;

import com.school.attendance.dto.*;
import com.school.attendance.entity.TeacherAssignment;
import com.school.attendance.entity.TeacherSchedule;
import com.school.attendance.entity.TeacherScheduleStatus;
import com.school.attendance.repository.TeacherAssignmentRepository;
import com.school.attendance.repository.TeacherScheduleRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/reports")
public class TeacherInsightController {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherScheduleRepository teacherScheduleRepository;

    public TeacherInsightController(
            TeacherAssignmentRepository teacherAssignmentRepository,
            TeacherScheduleRepository teacherScheduleRepository
    ) {
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.teacherScheduleRepository = teacherScheduleRepository;
    }

    @GetMapping("/teachers/search")
    public List<TeacherSearchDTO> searchTeachers(
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        return teacherAssignmentRepository.searchTeachers(keyword == null ? "" : keyword.trim());
    }

    @GetMapping("/teacher-insight/{teacherId}")
    public TeacherInsightSummaryDTO getTeacherInsight(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String rangeType
    ) {
        List<TeacherAssignment> assignments = teacherAssignmentRepository
                .findByTeacherIdOrderByClassNameAscSectionAscSubjectNameAsc(teacherId);

        List<TeacherSchedule> schedules = getTeacherSchedulesForRange(teacherId, fromDate, toDate);

        String teacherName = assignments.stream()
                .map(TeacherAssignment::getTeacherName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> schedules.stream()
                        .map(TeacherSchedule::getTeacherName)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("Teacher " + teacherId));

        int plannedLeaves = (int) schedules.stream()
                .filter(schedule -> schedule.getStatus() == TeacherScheduleStatus.PLANNED_LEAVE)
                .count();

        int unplannedLeaves = (int) schedules.stream()
                .filter(schedule -> schedule.getStatus() == TeacherScheduleStatus.UNPLANNED_LEAVE)
                .count();

        int replacementAssignments = getReplacementSchedulesForRange(teacherId, fromDate, toDate).size();

        TeacherInsightSummaryDTO dto = new TeacherInsightSummaryDTO();
        dto.setTeacherId(teacherId);
        dto.setTeacherName(teacherName);
        dto.setClassesHandled(assignments.stream()
                .map(TeacherAssignment::getClassName)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList());
        dto.setSectionsHandled(assignments.stream()
                .map(TeacherAssignment::getSection)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList());
        dto.setSubjectsHandled(assignments.stream()
                .map(TeacherAssignment::getSubjectName)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList());
        dto.setPlannedLeaves(plannedLeaves);
        dto.setUnplannedLeaves(unplannedLeaves);
        dto.setTotalLeaves(plannedLeaves + unplannedLeaves);
        dto.setReplacementAssignments(replacementAssignments);

        // Current source is TeacherSchedule rows. Replace later with true submitted attendance count
        // after AttendanceRepository submission audit/entity is available.
        dto.setAttendanceSubmissions(schedules.size());

        // Exam entity/repository is not connected yet.
        dto.setExamResultSubmissions(0);

        return dto;
    }

    @GetMapping("/teacher/{teacherId}/attendance-history")
    public List<TeacherAttendanceSubmissionDTO> getAttendanceHistory(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String rangeType
    ) {
        return getTeacherSchedulesForRange(teacherId, fromDate, toDate)
                .stream()
                .map(schedule -> {
                    TeacherAttendanceSubmissionDTO dto = new TeacherAttendanceSubmissionDTO();
                    dto.setScheduleId(schedule.getId());
                    dto.setAttendanceDate(schedule.getScheduleDate());
                    dto.setClassName(schedule.getClassName());
                    dto.setSection(schedule.getSection());
                    dto.setSubjectName(schedule.getSubjectName());
                    dto.setSubmittedTime(schedule.getStartTime());
                    dto.setStatus(schedule.getStatus() == null ? null : schedule.getStatus().name());
                    dto.setTotalStudents(null);
                    dto.setPresentStudents(null);
                    dto.setAbsentStudents(null);
                    return dto;
                })
                .toList();
    }

    @GetMapping("/teacher/{teacherId}/exam-history")
    public List<TeacherExamSubmissionDTO> getExamHistory(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String rangeType
    ) {
        // Wire this after sharing the Exam Result entity/repository.
        return List.of();
    }

    @GetMapping("/teacher/{teacherId}/leave-history")
    public List<TeacherLeaveHistoryDTO> getLeaveHistory(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String rangeType
    ) {
        return getTeacherSchedulesForRange(teacherId, fromDate, toDate)
                .stream()
                .filter(this::isLeaveSchedule)
                .map(schedule -> {
                    TeacherLeaveHistoryDTO dto = new TeacherLeaveHistoryDTO();
                    dto.setScheduleId(schedule.getId());
                    dto.setLeaveDate(schedule.getScheduleDate());
                    dto.setStartTime(schedule.getStartTime());
                    dto.setEndTime(schedule.getEndTime());
                    dto.setClassName(schedule.getClassName());
                    dto.setSection(schedule.getSection());
                    dto.setSubjectName(schedule.getSubjectName());
                    dto.setLeaveType(schedule.getStatus() == null ? null : schedule.getStatus().name());
                    dto.setReason(null);
                    dto.setStatus(schedule.getReplacementTeacherId() == null
                            ? "REPLACEMENT_PENDING"
                            : "REPLACEMENT_ASSIGNED");
                    dto.setReplacementTeacherId(schedule.getReplacementTeacherId());
                    dto.setReplacementTeacherName(schedule.getReplacementTeacherName());
                    return dto;
                })
                .toList();
    }

    @GetMapping("/teacher/{teacherId}/replacement-history")
    public List<TeacherReplacementHistoryDTO> getReplacementHistory(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String rangeType
    ) {
        return getReplacementSchedulesForRange(teacherId, fromDate, toDate)
                .stream()
                .map(schedule -> {
                    TeacherReplacementHistoryDTO dto = new TeacherReplacementHistoryDTO();
                    dto.setScheduleId(schedule.getId());
                    dto.setReplacementDate(schedule.getScheduleDate());
                    dto.setClassName(schedule.getClassName());
                    dto.setSection(schedule.getSection());
                    dto.setSubjectName(schedule.getSubjectName());
                    dto.setStartTime(schedule.getStartTime());
                    dto.setEndTime(schedule.getEndTime());
                    dto.setReplacedTeacherId(schedule.getTeacherId());
                    dto.setReplacedTeacherName(schedule.getTeacherName());
                    dto.setStatus(schedule.getStatus() == null ? null : schedule.getStatus().name());
                    return dto;
                })
                .toList();
    }


    @GetMapping("/teacher-monthly-overview")
    public Map<String, Object> getTeacherMonthlyOverview(@RequestParam String month) {
        YearMonth selectedMonth = YearMonth.parse(month);
        LocalDate from = selectedMonth.atDay(1);
        LocalDate to = selectedMonth.atEndOfMonth();

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();
        List<TeacherSchedule> schedules = teacherScheduleRepository
                .findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(from, to);

        Set<Long> teacherIds = assignments.stream()
                .map(TeacherAssignment::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (TeacherSchedule schedule : schedules) {
            if (schedule.getTeacherId() != null) {
                teacherIds.add(schedule.getTeacherId());
            }
            if (schedule.getReplacementTeacherId() != null) {
                teacherIds.add(schedule.getReplacementTeacherId());
            }
        }

        Set<Long> teachersInLeave = new LinkedHashSet<>();
        int plannedLeaves = 0;
        int unplannedLeaves = 0;

        for (TeacherSchedule schedule : schedules) {
            if (schedule.getStatus() == TeacherScheduleStatus.PLANNED_LEAVE) {
                plannedLeaves++;
                if (schedule.getTeacherId() != null) {
                    teachersInLeave.add(schedule.getTeacherId());
                }
            }

            if (schedule.getStatus() == TeacherScheduleStatus.UNPLANNED_LEAVE) {
                unplannedLeaves++;
                if (schedule.getTeacherId() != null) {
                    teachersInLeave.add(schedule.getTeacherId());
                }
            }
        }

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("month", month);
        overview.put("totalTeachers", teacherIds.size());
        overview.put("totalTeachersInLeave", teachersInLeave.size());
        overview.put("totalPlannedLeaves", plannedLeaves);
        overview.put("totalUnplannedLeaves", unplannedLeaves);
        return overview;
    }

    @GetMapping("/teacher-monthly-leaves")
    public List<Map<String, Object>> getTeacherMonthlyLeaves(@RequestParam String month) {
        YearMonth selectedMonth = YearMonth.parse(month);
        LocalDate from = selectedMonth.atDay(1);
        LocalDate to = selectedMonth.atEndOfMonth();

        List<TeacherSchedule> schedules = teacherScheduleRepository
                .findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(from, to);
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();

        Map<Long, List<TeacherAssignment>> assignmentsByTeacher = assignments.stream()
                .filter(assignment -> assignment.getTeacherId() != null)
                .collect(Collectors.groupingBy(
                        TeacherAssignment::getTeacherId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, Map<String, Object>> rows = new LinkedHashMap<>();

        for (TeacherSchedule schedule : schedules) {
            if (!isLeaveSchedule(schedule) || schedule.getTeacherId() == null) {
                continue;
            }

            Long teacherId = schedule.getTeacherId();
            Map<String, Object> row = rows.computeIfAbsent(teacherId, id -> {
                Map<String, Object> created = new LinkedHashMap<>();
                created.put("teacherId", id);
                created.put("teacherName", schedule.getTeacherName());
                created.put("totalLeaves", 0);
                created.put("plannedLeaves", 0);
                created.put("unplannedLeaves", 0);
                return created;
            });

            row.put("teacherName", schedule.getTeacherName());
            row.put("totalLeaves", getIntValue(row, "totalLeaves") + 1);

            if (schedule.getStatus() == TeacherScheduleStatus.PLANNED_LEAVE) {
                row.put("plannedLeaves", getIntValue(row, "plannedLeaves") + 1);
            }

            if (schedule.getStatus() == TeacherScheduleStatus.UNPLANNED_LEAVE) {
                row.put("unplannedLeaves", getIntValue(row, "unplannedLeaves") + 1);
            }
        }

        for (Map.Entry<Long, Map<String, Object>> entry : rows.entrySet()) {
            List<TeacherAssignment> teacherAssignments = assignmentsByTeacher.getOrDefault(entry.getKey(), List.of());
            entry.getValue().put("subjectsHandled", teacherAssignments.stream()
                    .map(TeacherAssignment::getSubjectName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList());
            entry.getValue().put("classesHandled", teacherAssignments.stream()
                    .map(TeacherAssignment::getClassName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList());
        }

        return rows.values()
                .stream()
                .sorted(Comparator
                        .comparingInt((Map<String, Object> row) -> getIntValue(row, "totalLeaves"))
                        .reversed()
                        .thenComparing(row -> String.valueOf(row.get("teacherName"))))
                .toList();
    }

    @GetMapping("/teacher-monthly-replacement-coverage")
    public List<Map<String, Object>> getTeacherMonthlyReplacementCoverage(@RequestParam String month) {
        YearMonth selectedMonth = YearMonth.parse(month);
        LocalDate from = selectedMonth.atDay(1);
        LocalDate to = selectedMonth.atEndOfMonth();

        List<TeacherSchedule> schedules = teacherScheduleRepository
                .findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(from, to);

        Map<Long, Map<String, Object>> rows = new LinkedHashMap<>();

        for (TeacherSchedule schedule : schedules) {
            if (schedule.getReplacementTeacherId() == null) {
                continue;
            }

            Long teacherId = schedule.getReplacementTeacherId();
            Map<String, Object> row = rows.computeIfAbsent(teacherId, id -> {
                Map<String, Object> created = new LinkedHashMap<>();
                created.put("teacherId", id);
                created.put("teacherName", schedule.getReplacementTeacherName());
                created.put("totalReplacementPeriods", 0);
                created.put("totalMinutes", 0);
                created.put("classKeys", new LinkedHashSet<String>());
                created.put("subjectKeys", new LinkedHashSet<String>());
                return created;
            });

            row.put("teacherName", schedule.getReplacementTeacherName());
            row.put("totalReplacementPeriods", getIntValue(row, "totalReplacementPeriods") + 1);

            if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
                row.put("totalMinutes", getIntValue(row, "totalMinutes")
                        + (int) Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes());
            }

            getStringSet(row, "classKeys").add(schedule.getClassName() + "-" + schedule.getSection());
            getStringSet(row, "subjectKeys").add(schedule.getSubjectName());
        }

        return rows.values()
                .stream()
                .peek(row -> {
                    Set<String> classKeys = getStringSet(row, "classKeys");
                    Set<String> subjectKeys = getStringSet(row, "subjectKeys");
                    int totalMinutes = getIntValue(row, "totalMinutes");
                    row.put("classesCovered", classKeys.size());
                    row.put("subjectsCovered", subjectKeys.size());
                    row.put("hours", Math.round((totalMinutes / 60.0) * 10.0) / 10.0);
                    row.remove("classKeys");
                    row.remove("subjectKeys");
                })
                .sorted(Comparator
                        .comparingInt((Map<String, Object> row) -> getIntValue(row, "totalReplacementPeriods"))
                        .reversed()
                        .thenComparing(row -> String.valueOf(row.get("teacherName"))))
                .toList();
    }

    private List<TeacherSchedule> getTeacherSchedulesForRange(
            Long teacherId,
            String fromDate,
            String toDate
    ) {
        if (hasDateRange(fromDate, toDate)) {
            return teacherScheduleRepository.findByTeacherIdAndScheduleDateBetweenOrderByScheduleDateDescStartTimeAsc(
                    teacherId,
                    LocalDate.parse(fromDate),
                    LocalDate.parse(toDate)
            );
        }

        return teacherScheduleRepository.findByTeacherIdOrderByScheduleDateDescStartTimeAsc(teacherId);
    }

    private List<TeacherSchedule> getReplacementSchedulesForRange(
            Long replacementTeacherId,
            String fromDate,
            String toDate
    ) {
        if (hasDateRange(fromDate, toDate)) {
            return teacherScheduleRepository.findByReplacementTeacherIdAndScheduleDateBetweenOrderByScheduleDateDescStartTimeAsc(
                    replacementTeacherId,
                    LocalDate.parse(fromDate),
                    LocalDate.parse(toDate)
            );
        }

        return teacherScheduleRepository.findByReplacementTeacherIdOrderByScheduleDateDescStartTimeAsc(replacementTeacherId);
    }


    private int getIntValue(Map<String, Object> row, String key) {
        Object value = row.get(key);

        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getStringSet(Map<String, Object> row, String key) {
        Object value = row.get(key);

        if (value instanceof Set<?>) {
            return (Set<String>) value;
        }

        Set<String> created = new LinkedHashSet<>();
        row.put(key, created);
        return created;
    }

    private boolean hasDateRange(String fromDate, String toDate) {
        return fromDate != null && !fromDate.isBlank()
                && toDate != null && !toDate.isBlank();
    }

    private boolean isLeaveSchedule(TeacherSchedule schedule) {
        return schedule.getStatus() == TeacherScheduleStatus.PLANNED_LEAVE
                || schedule.getStatus() == TeacherScheduleStatus.UNPLANNED_LEAVE;
    }
}
