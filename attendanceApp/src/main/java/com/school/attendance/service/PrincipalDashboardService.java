package com.school.attendance.service;

import com.school.attendance.entity.*;
import com.school.attendance.dto.ClassComparisonDTO;
import com.school.attendance.dto.ExecutiveOverviewDTO;
import com.school.attendance.dto.PrincipalDashboardSummaryDTO;
import com.school.attendance.dto.PrincipalRiskAlertDTO;
import com.school.attendance.dto.TeacherWorkloadDTO;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.TeacherScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrincipalDashboardService {

    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;
    private final AttendanceRepository attendanceRepository;
    private final TeacherScheduleRepository teacherScheduleRepository;

    public PrincipalDashboardService(StudentRepository studentRepository, AppUserRepository appUserRepository, AttendanceRepository attendanceRepository, TeacherScheduleRepository teacherScheduleRepository) {
        this.studentRepository = studentRepository;
        this.appUserRepository = appUserRepository;
        this.attendanceRepository = attendanceRepository;
        this.teacherScheduleRepository = teacherScheduleRepository;
    }

    public PrincipalDashboardSummaryDTO getSummary(LocalDate date) {
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        List<Student> students = studentRepository.findAll();
        List<AppUser> teachers = appUserRepository.findByRoleIgnoreCase("TEACHER");
        List<Attendance> todayAttendance = attendanceRepository.findByAttendanceDate(selectedDate);
        List<TeacherSchedule> todaySchedules = teacherScheduleRepository.findByScheduleDate(selectedDate);

        long present = todayAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = todayAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long totalMarked = present + absent;
        double percentage = totalMarked == 0 ? 0.0 : round((present * 100.0) / totalMarked);

        long teachersOnLeave = todaySchedules.stream()
                .filter(s -> s.getStatus() == TeacherScheduleStatus.PLANNED_LEAVE || s.getStatus() == TeacherScheduleStatus.UNPLANNED_LEAVE)
                .map(TeacherSchedule::getTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long replacementPeriods = todaySchedules.stream()
                .filter(s -> s.getReplacementTeacherId() != null || Boolean.TRUE.equals(s.getReplacementClass()))
                .count();

        long pendingTeacherAttendance = Math.max(0, todaySchedules.size() - todayAttendance.stream()
                .map(a -> safe(a.getTeacherId()) + "|" + safeText(a.getClassName()) + "|" + safeText(a.getSection()) + "|" + safeText(a.getSubjectName()))
                .collect(Collectors.toSet()).size());

        long lowAttendanceStudents = countLowAttendanceStudents(YearMonth.from(selectedDate), 60.0);

        return new PrincipalDashboardSummaryDTO(
                students.size(),
                teachers.size(),
                percentage,
                absent,
                teachersOnLeave,
                replacementPeriods,
                lowAttendanceStudents,
                pendingTeacherAttendance
        );
    }

    public List<PrincipalRiskAlertDTO> getRiskAlerts(String month) {
        YearMonth yearMonth = parseMonth(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<PrincipalRiskAlertDTO> alerts = new ArrayList<>();

        buildStudentRiskAlerts(start, end, alerts);
        buildTeacherLeaveAlerts(start, end, alerts);
        buildReplacementLoadAlerts(start, end, alerts);
        buildClassAttendanceAlerts(start, end, alerts);

        return alerts.stream()
                .sorted(Comparator.comparing(PrincipalRiskAlertDTO::getSeverity).thenComparing(PrincipalRiskAlertDTO::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(20)
                .collect(Collectors.toList());
    }

    public List<ClassComparisonDTO> getClassComparison(String month, String className, String section, String classA, String classB) {
        YearMonth yearMonth = parseMonth(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        Set<String> selectedKeys = new HashSet<>();
        addClassKey(selectedKeys, className, section);
        addCompactClassKey(selectedKeys, classA);
        addCompactClassKey(selectedKeys, classB);

        List<Attendance> records = attendanceRepository.findByAttendanceDateBetween(start, end).stream()
                .filter(a -> a.getStudent() != null || safeText(a.getClassName()).length() > 0)
                .filter(a -> selectedKeys.isEmpty() || selectedKeys.contains(key(resolveClassName(a), resolveSection(a))))
                .collect(Collectors.toList());

        Map<String, List<Attendance>> grouped = records.stream()
                .collect(Collectors.groupingBy(a -> key(resolveClassName(a), resolveSection(a)), TreeMap::new, Collectors.toList()));

        List<ClassComparisonDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<Attendance>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split("\\|", -1);
            List<Attendance> list = entry.getValue();
            long present = list.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long absent = list.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
            long total = present + absent;
            result.add(new ClassComparisonDTO(parts.length > 0 ? parts[0] : "", parts.length > 1 ? parts[1] : "", present, absent, total, total == 0 ? 0.0 : round((present * 100.0) / total)));
        }
        return result;
    }


    public ExecutiveOverviewDTO getExecutiveOverview(String month) {
        YearMonth yearMonth = parseMonth(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Attendance> records = attendanceRepository.findByAttendanceDateBetween(start, end);
        long present = records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = records.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long total = present + absent;
        double overallAttendance = total == 0 ? 0.0 : round((present * 100.0) / total);

        List<ClassComparisonDTO> classComparisons = getClassComparison(month, null, null, null, null);
        long classesBelowThreshold = classComparisons.stream().filter(c -> c.getAttendancePercentage() > 0 && c.getAttendancePercentage() < 75.0).count();
        String topClass = classComparisons.stream()
                .max(Comparator.comparing(ClassComparisonDTO::getAttendancePercentage))
                .map(c -> safeClassLabel(c.getClassName(), c.getSection()))
                .orElse("No data");
        String weakestSection = classComparisons.stream()
                .filter(c -> c.getTotalMarked() > 0)
                .min(Comparator.comparing(ClassComparisonDTO::getAttendancePercentage))
                .map(c -> safeClassLabel(c.getClassName(), c.getSection()))
                .orElse("No data");

        List<TeacherWorkloadDTO> workload = getTeacherWorkload(month);
        long teachersWithLeaveLoad = workload.stream().filter(t -> t.getPlannedLeaves() + t.getUnplannedLeaves() >= 3).count();
        long replacementStressTeachers = workload.stream().filter(t -> t.getReplacementPeriods() >= 5 || "HIGH".equals(t.getRiskLevel())).count();
        double replacementStressIndex = workload.isEmpty() ? 0.0 : round(workload.stream().mapToDouble(TeacherWorkloadDTO::getWorkloadScore).average().orElse(0.0));

        return new ExecutiveOverviewDTO(
                overallAttendance,
                countLowAttendanceStudents(yearMonth, 75.0),
                classesBelowThreshold,
                teachersWithLeaveLoad,
                replacementStressTeachers,
                getRiskAlerts(month).stream().filter(a -> "HIGH".equals(a.getSeverity())).count(),
                topClass,
                weakestSection,
                replacementStressIndex
        );
    }

    public List<TeacherWorkloadDTO> getTeacherWorkload(String month) {
        YearMonth yearMonth = parseMonth(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<TeacherSchedule> schedules = teacherScheduleRepository.findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(start, end);
        List<AppUser> teachers = appUserRepository.findByRoleIgnoreCase("TEACHER");

        Map<Long, String> teacherNames = new LinkedHashMap<>();
        for (AppUser teacher : teachers) {
            Long id = teacher.getTeacherId() != null ? teacher.getTeacherId() : teacher.getId();
            if (id != null) teacherNames.put(id, safeText(teacher.getTeacherName()).isBlank() ? safeText(teacher.getUsername()) : safeText(teacher.getTeacherName()));
        }
        for (TeacherSchedule schedule : schedules) {
            if (schedule.getTeacherId() != null) teacherNames.putIfAbsent(schedule.getTeacherId(), safeText(schedule.getTeacherName()));
            if (schedule.getReplacementTeacherId() != null) teacherNames.putIfAbsent(schedule.getReplacementTeacherId(), safeText(schedule.getReplacementTeacherName()));
        }

        List<TeacherWorkloadDTO> result = new ArrayList<>();
        for (Map.Entry<Long, String> entry : teacherNames.entrySet()) {
            Long teacherId = entry.getKey();
            long scheduled = schedules.stream().filter(s -> teacherId.equals(s.getTeacherId())).count();
            long replacement = schedules.stream().filter(s -> teacherId.equals(s.getReplacementTeacherId())).count();
            long planned = schedules.stream().filter(s -> teacherId.equals(s.getTeacherId()) && s.getStatus() == TeacherScheduleStatus.PLANNED_LEAVE).map(TeacherSchedule::getScheduleDate).distinct().count();
            long unplanned = schedules.stream().filter(s -> teacherId.equals(s.getTeacherId()) && s.getStatus() == TeacherScheduleStatus.UNPLANNED_LEAVE).map(TeacherSchedule::getScheduleDate).distinct().count();
            double score = round(scheduled + (replacement * 1.5) + (unplanned * 2.0));
            String risk = score >= 45 || replacement >= 12 || unplanned >= 4 ? "HIGH" : (score >= 25 || replacement >= 5 || planned + unplanned >= 3 ? "MEDIUM" : "LOW");
            result.add(new TeacherWorkloadDTO(teacherId, entry.getValue(), scheduled, replacement, planned, unplanned, score, risk));
        }
        return result.stream()
                .sorted(Comparator.comparing(TeacherWorkloadDTO::getWorkloadScore).reversed())
                .collect(Collectors.toList());
    }

    public List<PrincipalRiskAlertDTO> getExecutiveAlerts(String month) {
        List<PrincipalRiskAlertDTO> alerts = new ArrayList<>(getRiskAlerts(month));
        getTeacherWorkload(month).stream().limit(5).forEach(t -> {
            if (!"LOW".equals(t.getRiskLevel())) {
                alerts.add(new PrincipalRiskAlertDTO("TEACHER_WORKLOAD", t.getTeacherName(), "Workload score " + t.getWorkloadScore() + " with " + t.getReplacementPeriods() + " replacement period(s)", t.getRiskLevel(), t.getWorkloadScore()));
            }
        });
        return alerts.stream()
                .sorted(Comparator.comparing(PrincipalRiskAlertDTO::getSeverity).thenComparing(PrincipalRiskAlertDTO::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(25)
                .collect(Collectors.toList());
    }

    private void buildStudentRiskAlerts(LocalDate start, LocalDate end, List<PrincipalRiskAlertDTO> alerts) {
        Map<Long, List<Attendance>> byStudent = attendanceRepository.findByAttendanceDateBetween(start, end).stream()
                .filter(a -> a.getStudent() != null)
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        for (Map.Entry<Long, List<Attendance>> entry : byStudent.entrySet()) {
            List<Attendance> list = entry.getValue();
            long present = list.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long absent = list.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
            long total = present + absent;
            if (total == 0) continue;
            double percent = round((present * 100.0) / total);
            if (percent < 60.0) {
                Student student = list.get(0).getStudent();
                alerts.add(new PrincipalRiskAlertDTO("LOW_ATTENDANCE", student.getName(), "Attendance is " + percent + "% for " + safeText(student.getClassName()) + "-" + safeText(student.getSection()), percent < 50 ? "HIGH" : "MEDIUM", percent));
            }
        }
    }

    private void buildTeacherLeaveAlerts(LocalDate start, LocalDate end, List<PrincipalRiskAlertDTO> alerts) {
        Map<Long, List<TeacherSchedule>> leavesByTeacher = teacherScheduleRepository.findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(start, end).stream()
                .filter(s -> s.getStatus() == TeacherScheduleStatus.PLANNED_LEAVE || s.getStatus() == TeacherScheduleStatus.UNPLANNED_LEAVE)
                .filter(s -> s.getTeacherId() != null)
                .collect(Collectors.groupingBy(TeacherSchedule::getTeacherId));

        for (List<TeacherSchedule> leaves : leavesByTeacher.values()) {
            long leaveDays = leaves.stream().map(TeacherSchedule::getScheduleDate).distinct().count();
            if (leaveDays >= 3) {
                TeacherSchedule sample = leaves.get(0);
                alerts.add(new PrincipalRiskAlertDTO("TEACHER_LEAVE_LOAD", safeText(sample.getTeacherName()), leaveDays + " leave day(s) in selected month", leaveDays >= 5 ? "HIGH" : "MEDIUM", (double) leaveDays));
            }
        }
    }

    private void buildReplacementLoadAlerts(LocalDate start, LocalDate end, List<PrincipalRiskAlertDTO> alerts) {
        Map<Long, List<TeacherSchedule>> byReplacementTeacher = teacherScheduleRepository.findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(start, end).stream()
                .filter(s -> s.getReplacementTeacherId() != null)
                .collect(Collectors.groupingBy(TeacherSchedule::getReplacementTeacherId));

        for (List<TeacherSchedule> periods : byReplacementTeacher.values()) {
            if (periods.size() >= 5) {
                TeacherSchedule sample = periods.get(0);
                alerts.add(new PrincipalRiskAlertDTO("REPLACEMENT_OVERLOAD", safeText(sample.getReplacementTeacherName()), periods.size() + " replacement period(s) in selected month", periods.size() >= 10 ? "HIGH" : "MEDIUM", (double) periods.size()));
            }
        }
    }

    private void buildClassAttendanceAlerts(LocalDate start, LocalDate end, List<PrincipalRiskAlertDTO> alerts) {
        Map<String, List<Attendance>> byClass = attendanceRepository.findByAttendanceDateBetween(start, end).stream()
                .collect(Collectors.groupingBy(a -> key(resolveClassName(a), resolveSection(a))));

        for (Map.Entry<String, List<Attendance>> entry : byClass.entrySet()) {
            List<Attendance> list = entry.getValue();
            long present = list.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long absent = list.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
            long total = present + absent;
            if (total == 0) continue;
            double percent = round((present * 100.0) / total);
            if (percent < 75.0) {
                alerts.add(new PrincipalRiskAlertDTO("CLASS_ATTENDANCE_RISK", entry.getKey().replace("|", "-"), "Class attendance is " + percent + "%", percent < 65 ? "HIGH" : "MEDIUM", percent));
            }
        }
    }

    private long countLowAttendanceStudents(YearMonth yearMonth, double threshold) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        Map<Long, List<Attendance>> byStudent = attendanceRepository.findByAttendanceDateBetween(start, end).stream()
                .filter(a -> a.getStudent() != null)
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));
        return byStudent.values().stream().filter(list -> {
            long present = list.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long absent = list.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
            long total = present + absent;
            return total > 0 && ((present * 100.0) / total) < threshold;
        }).count();
    }

    private YearMonth parseMonth(String month) {
        return month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    }

    private void addClassKey(Set<String> selectedKeys, String className, String section) {
        if (!safeText(className).isBlank()) selectedKeys.add(key(className, section));
    }

    private void addCompactClassKey(Set<String> selectedKeys, String compact) {
        String value = safeText(compact).trim();
        if (value.isEmpty()) return;
        String cleaned = value.replace("-", "").replace(" ", "");
        if (cleaned.length() >= 2) selectedKeys.add(key(cleaned.substring(0, cleaned.length() - 1), cleaned.substring(cleaned.length() - 1)));
    }

    private String resolveClassName(Attendance attendance) {
        if (attendance.getStudent() != null && !safeText(attendance.getStudent().getClassName()).isBlank()) return safeText(attendance.getStudent().getClassName());
        return safeText(attendance.getClassName());
    }

    private String resolveSection(Attendance attendance) {
        if (attendance.getStudent() != null && !safeText(attendance.getStudent().getSection()).isBlank()) return safeText(attendance.getStudent().getSection());
        return safeText(attendance.getSection());
    }

    private String key(String className, String section) {
        return safeText(className).trim() + "|" + safeText(section).trim();
    }

    private String safeClassLabel(String className, String section) {
        String c = safeText(className).trim();
        String s = safeText(section).trim();
        return s.isBlank() ? c : c + "-" + s;
    }

    private String safeText(String value) { return value == null ? "" : value; }
    private Long safe(Long value) { return value == null ? 0L : value; }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
