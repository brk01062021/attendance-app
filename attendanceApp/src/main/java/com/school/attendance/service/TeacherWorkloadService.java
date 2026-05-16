package com.school.attendance.service;

import com.school.attendance.dto.TeacherFatigueAlertDTO;
import com.school.attendance.dto.TeacherReplacementLoadDTO;
import com.school.attendance.dto.TeacherWorkloadInsightDTO;
import com.school.attendance.entity.TeacherSchedule;
import com.school.attendance.entity.TeacherScheduleStatus;
import com.school.attendance.repository.TeacherScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TeacherWorkloadService {

    private static final int SAFE_DAILY_PERIODS = 6;
    private static final int HIGH_RISK_SCORE = 80;
    private static final int MEDIUM_RISK_SCORE = 55;

    private final TeacherScheduleRepository teacherScheduleRepository;

    public TeacherWorkloadService(TeacherScheduleRepository teacherScheduleRepository) {
        this.teacherScheduleRepository = teacherScheduleRepository;
    }

    public List<TeacherWorkloadInsightDTO> getDailySummary(LocalDate date) {
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        List<TeacherSchedule> schedules = teacherScheduleRepository.findByScheduleDateOrderByTeacherNameAscStartTimeAsc(selectedDate);
        Map<Long, List<TeacherSchedule>> grouped = schedules.stream()
                .filter(schedule -> schedule.getTeacherId() != null)
                .collect(Collectors.groupingBy(TeacherSchedule::getTeacherId, LinkedHashMap::new, Collectors.toList()));

        List<TeacherWorkloadInsightDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<TeacherSchedule>> entry : grouped.entrySet()) {
            result.add(buildInsight(entry.getKey(), entry.getValue().get(0).getTeacherName(), selectedDate, schedules));
        }

        addReplacementOnlyTeachers(result, selectedDate, schedules);

        return result.stream()
                .sorted(Comparator.comparingInt(TeacherWorkloadInsightDTO::getOverloadScore).reversed()
                        .thenComparing(TeacherWorkloadInsightDTO::getTeacherName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public List<TeacherFatigueAlertDTO> getFatigueAlerts(LocalDate date) {
        return getDailySummary(date).stream()
                .filter(item -> item.getOverloadScore() >= MEDIUM_RISK_SCORE)
                .map(item -> new TeacherFatigueAlertDTO(
                        item.getTeacherId(),
                        item.getTeacherName(),
                        item.getDate(),
                        item.getRiskLevel(),
                        item.getOverloadScore(),
                        buildFatigueReason(item),
                        buildAction(item)
                ))
                .toList();
    }

    public TeacherWorkloadInsightDTO getTeacherDailyInsight(Long teacherId, LocalDate date) {
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        List<TeacherSchedule> schedules = teacherScheduleRepository.findByScheduleDateOrderByTeacherNameAscStartTimeAsc(selectedDate);
        String teacherName = schedules.stream()
                .filter(schedule -> Objects.equals(schedule.getTeacherId(), teacherId) || Objects.equals(schedule.getReplacementTeacherId(), teacherId))
                .map(schedule -> Objects.equals(schedule.getReplacementTeacherId(), teacherId) ? schedule.getReplacementTeacherName() : schedule.getTeacherName())
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElse("Teacher");
        return buildInsight(teacherId, teacherName, selectedDate, schedules);
    }

    public List<TeacherReplacementLoadDTO> getReplacementLoad(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = fromDate == null ? LocalDate.now().withDayOfMonth(1) : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        List<TeacherSchedule> schedules = teacherScheduleRepository.findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(from, to);

        Map<Long, String> teacherNames = new LinkedHashMap<>();
        for (TeacherSchedule schedule : schedules) {
            if (schedule.getTeacherId() != null) {
                teacherNames.putIfAbsent(schedule.getTeacherId(), safe(schedule.getTeacherName()));
            }
            if (schedule.getReplacementTeacherId() != null) {
                teacherNames.putIfAbsent(schedule.getReplacementTeacherId(), safe(schedule.getReplacementTeacherName()));
            }
        }

        List<TeacherReplacementLoadDTO> result = new ArrayList<>();
        for (Map.Entry<Long, String> entry : teacherNames.entrySet()) {
            Long teacherId = entry.getKey();
            int scheduled = (int) schedules.stream().filter(schedule -> Objects.equals(schedule.getTeacherId(), teacherId)).count();
            int replacements = (int) schedules.stream().filter(schedule -> Objects.equals(schedule.getReplacementTeacherId(), teacherId)).count();
            int leaves = (int) schedules.stream().filter(schedule -> Objects.equals(schedule.getTeacherId(), teacherId) && isLeave(schedule)).count();
            int overloadScore = calculateRangeScore(scheduled, replacements, leaves);
            result.add(new TeacherReplacementLoadDTO(teacherId, entry.getValue(), scheduled, replacements, leaves, overloadScore, riskLevel(overloadScore)));
        }

        return result.stream()
                .sorted(Comparator.comparingInt(TeacherReplacementLoadDTO::getOverloadScore).reversed()
                        .thenComparing(TeacherReplacementLoadDTO::getTeacherName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private TeacherWorkloadInsightDTO buildInsight(Long teacherId, String teacherName, LocalDate date, List<TeacherSchedule> allSchedulesForDate) {
        List<TeacherSchedule> ownSchedules = allSchedulesForDate.stream()
                .filter(schedule -> Objects.equals(schedule.getTeacherId(), teacherId))
                .sorted(Comparator.comparing(TeacherSchedule::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<TeacherSchedule> replacementSchedules = allSchedulesForDate.stream()
                .filter(schedule -> Objects.equals(schedule.getReplacementTeacherId(), teacherId))
                .toList();

        int scheduledPeriods = ownSchedules.size();
        int replacementPeriods = replacementSchedules.size();
        int totalPeriods = scheduledPeriods + replacementPeriods;
        int consecutivePeriods = calculateMaxConsecutivePeriods(ownSchedules);
        int freePeriodGaps = Math.max(0, scheduledPeriods - consecutivePeriods);
        int overloadScore = calculateDailyScore(totalPeriods, replacementPeriods, consecutivePeriods, freePeriodGaps);
        String riskLevel = riskLevel(overloadScore);

        return new TeacherWorkloadInsightDTO(
                teacherId,
                safe(teacherName),
                date,
                scheduledPeriods,
                replacementPeriods,
                totalPeriods,
                consecutivePeriods,
                freePeriodGaps,
                overloadScore,
                riskLevel,
                buildRecommendation(riskLevel, totalPeriods, replacementPeriods)
        );
    }

    private void addReplacementOnlyTeachers(List<TeacherWorkloadInsightDTO> result, LocalDate date, List<TeacherSchedule> schedules) {
        List<Long> existingIds = result.stream().map(TeacherWorkloadInsightDTO::getTeacherId).toList();
        schedules.stream()
                .filter(schedule -> schedule.getReplacementTeacherId() != null && !existingIds.contains(schedule.getReplacementTeacherId()))
                .collect(Collectors.toMap(TeacherSchedule::getReplacementTeacherId, TeacherSchedule::getReplacementTeacherName, (a, b) -> a, LinkedHashMap::new))
                .forEach((teacherId, teacherName) -> result.add(buildInsight(teacherId, teacherName, date, schedules)));
    }

    private int calculateMaxConsecutivePeriods(List<TeacherSchedule> schedules) {
        if (schedules.isEmpty()) {
            return 0;
        }
        int max = 1;
        int current = 1;
        for (int index = 1; index < schedules.size(); index++) {
            TeacherSchedule previous = schedules.get(index - 1);
            TeacherSchedule next = schedules.get(index);
            if (previous.getEndTime() != null && next.getStartTime() != null && !next.getStartTime().isAfter(previous.getEndTime())) {
                current++;
            } else {
                current = 1;
            }
            max = Math.max(max, current);
        }
        return max;
    }

    private int calculateDailyScore(int totalPeriods, int replacementPeriods, int consecutivePeriods, int freePeriodGaps) {
        int score = totalPeriods * 10 + replacementPeriods * 12 + Math.max(0, consecutivePeriods - 3) * 10;
        if (totalPeriods > SAFE_DAILY_PERIODS) {
            score += (totalPeriods - SAFE_DAILY_PERIODS) * 8;
        }
        if (freePeriodGaps <= 1 && totalPeriods >= 5) {
            score += 8;
        }
        return Math.min(100, score);
    }

    private int calculateRangeScore(int scheduledPeriods, int replacementPeriods, int leavePeriods) {
        return Math.min(100, scheduledPeriods + replacementPeriods * 8 + leavePeriods * 5);
    }

    private String riskLevel(int score) {
        if (score >= HIGH_RISK_SCORE) {
            return "HIGH";
        }
        if (score >= MEDIUM_RISK_SCORE) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String buildRecommendation(String riskLevel, int totalPeriods, int replacementPeriods) {
        if ("HIGH".equals(riskLevel)) {
            return "Do not assign more replacement periods today; review timetable balance.";
        }
        if ("MEDIUM".equals(riskLevel)) {
            return replacementPeriods > 0 ? "Avoid additional replacement load unless urgent." : "Monitor before approving extra duties.";
        }
        return "Safe for normal timetable operations.";
    }

    private String buildFatigueReason(TeacherWorkloadInsightDTO item) {
        return item.getTotalPeriods() + " total period(s), " + item.getReplacementPeriods() + " replacement period(s), max " + item.getConsecutivePeriods() + " consecutive period(s).";
    }

    private String buildAction(TeacherWorkloadInsightDTO item) {
        if ("HIGH".equals(item.getRiskLevel())) {
            return "Principal/admin should avoid assigning additional replacement classes.";
        }
        return "Use only after best-match and lower-load teachers are checked.";
    }

    private boolean isLeave(TeacherSchedule schedule) {
        return schedule.getStatus() == TeacherScheduleStatus.PLANNED_LEAVE || schedule.getStatus() == TeacherScheduleStatus.UNPLANNED_LEAVE;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Teacher" : value;
    }
}
