package com.school.attendance.analytics.service;

import com.school.attendance.analytics.dto.AnalyticsSummaryDTO;
import com.school.attendance.analytics.dto.AttendanceTrendDTO;
import com.school.attendance.analytics.dto.ClassAttendanceTrendDTO;
import com.school.attendance.analytics.dto.TeacherReplacementTrendDTO;
import com.school.attendance.dto.SectionAnalyticsDTO;
import com.school.attendance.entity.Attendance;
import com.school.attendance.entity.AttendanceStatus;
import com.school.attendance.entity.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.TeacherScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final TeacherScheduleRepository teacherScheduleRepository;

    public AnalyticsService(
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            TeacherScheduleRepository teacherScheduleRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.teacherScheduleRepository = teacherScheduleRepository;
    }

    public AnalyticsSummaryDTO getSummary(LocalDate startDate, LocalDate endDate) {

        List<Attendance> attendanceList =
                attendanceRepository.findByAttendanceDateBetween(startDate, endDate);

        List<Student> students = studentRepository.findAll();

        long presentCount = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absentCount = attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long totalMarked = presentCount + absentCount;

        double attendancePercentage =
                totalMarked == 0
                        ? 0.0
                        : roundToTwoDecimals((presentCount * 100.0) / totalMarked);

        return new AnalyticsSummaryDTO(
                (long) students.size(),
                presentCount,
                absentCount,
                attendancePercentage,
                0L,
                0L,
                0L
        );
    }

    public List<AttendanceTrendDTO> getAttendanceTrend(
            LocalDate startDate,
            LocalDate endDate
    ) {

        List<Attendance> attendanceList =
                attendanceRepository.findByAttendanceDateBetween(startDate, endDate);

        Map<LocalDate, List<Attendance>> groupedByDate =
                attendanceList.stream()
                        .collect(Collectors.groupingBy(Attendance::getAttendanceDate));

        List<AttendanceTrendDTO> trend = new ArrayList<>();

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {

            List<Attendance> dayRecords =
                    groupedByDate.getOrDefault(currentDate, Collections.emptyList());

            long presentCount = dayRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absentCount = dayRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long totalCount = presentCount + absentCount;

            double percentage =
                    totalCount == 0
                            ? 0.0
                            : roundToTwoDecimals((presentCount * 100.0) / totalCount);

            trend.add(new AttendanceTrendDTO(
                    currentDate.toString(),
                    presentCount,
                    absentCount,
                    totalCount,
                    percentage
            ));

            currentDate = currentDate.plusDays(1);
        }

        return trend;
    }

    public List<ClassAttendanceTrendDTO> getClassAttendanceTrend(LocalDate date) {

        List<Attendance> attendanceList =
                attendanceRepository.findByAttendanceDate(date);

        Map<String, List<Attendance>> groupedByClassSection =
                attendanceList.stream()
                        .filter(a -> a.getStudent() != null)
                        .collect(Collectors.groupingBy(a ->
                                safeText(a.getStudent().getClassName())
                                        + "||"
                                        + safeText(a.getStudent().getSection())
                        ));

        List<ClassAttendanceTrendDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<Attendance>> entry : groupedByClassSection.entrySet()) {

            String[] parts = entry.getKey().split("\\|\\|");

            String className = parts[0];
            String section = parts[1];

            List<Attendance> records = entry.getValue();

            long presentCount = records.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absentCount = records.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long totalCount = presentCount + absentCount;

            double percentage =
                    totalCount == 0
                            ? 0.0
                            : roundToTwoDecimals((presentCount * 100.0) / totalCount);

            result.add(new ClassAttendanceTrendDTO(
                    className,
                    section,
                    presentCount,
                    absentCount,
                    totalCount,
                    percentage
            ));
        }

        return result;
    }


    public List<AttendanceTrendDTO> getMonthlyAttendanceTrend(
            String month,
            String className,
            String section
    ) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Attendance> attendanceList =
                attendanceRepository.findByAttendanceDateBetween(startDate, endDate);

        String selectedClass = safeText(className).trim();
        String selectedSection = safeText(section).trim();

        if (!selectedClass.isEmpty() || !selectedSection.isEmpty()) {
            attendanceList = attendanceList.stream()
                    .filter(a -> a.getStudent() != null)
                    .filter(a -> selectedClass.isEmpty()
                            || safeText(a.getStudent().getClassName()).equalsIgnoreCase(selectedClass))
                    .filter(a -> selectedSection.isEmpty()
                            || safeText(a.getStudent().getSection()).equalsIgnoreCase(selectedSection))
                    .collect(Collectors.toList());
        }

        Map<LocalDate, List<Attendance>> groupedByDate =
                attendanceList.stream()
                        .collect(Collectors.groupingBy(Attendance::getAttendanceDate));

        List<AttendanceTrendDTO> trend = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            List<Attendance> dayRecords =
                    groupedByDate.getOrDefault(currentDate, Collections.emptyList());

            long presentCount = dayRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absentCount = dayRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long totalCount = presentCount + absentCount;

            double percentage =
                    totalCount == 0
                            ? 0.0
                            : roundToTwoDecimals((presentCount * 100.0) / totalCount);

            trend.add(new AttendanceTrendDTO(
                    currentDate.toString(),
                    presentCount,
                    absentCount,
                    totalCount,
                    percentage
            ));

            currentDate = currentDate.plusDays(1);
        }

        return trend;
    }

    public List<ClassAttendanceTrendDTO> getMonthlyClassComparison(String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Attendance> attendanceList =
                attendanceRepository.findByAttendanceDateBetween(startDate, endDate);

        Map<String, List<Attendance>> groupedByClassSection =
                attendanceList.stream()
                        .filter(a -> a.getStudent() != null)
                        .collect(Collectors.groupingBy(a ->
                                safeText(a.getStudent().getClassName())
                                        + "||"
                                        + safeText(a.getStudent().getSection())
                        ));

        List<ClassAttendanceTrendDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<Attendance>> entry : groupedByClassSection.entrySet()) {
            String[] parts = entry.getKey().split("\\|\\|", -1);

            String className = parts.length > 0 ? parts[0] : "";
            String section = parts.length > 1 ? parts[1] : "";

            List<Attendance> records = entry.getValue();

            long presentCount = records.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absentCount = records.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long totalCount = presentCount + absentCount;

            double percentage =
                    totalCount == 0
                            ? 0.0
                            : roundToTwoDecimals((presentCount * 100.0) / totalCount);

            result.add(new ClassAttendanceTrendDTO(
                    className,
                    section,
                    presentCount,
                    absentCount,
                    totalCount,
                    percentage
            ));
        }

        return result;
    }


    public List<SectionAnalyticsDTO> getMonthlySectionComparison(String month, String className) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        String selectedClass = safeText(className).trim();

        List<Student> students = studentRepository.findAll().stream()
                .filter(student -> selectedClass.isEmpty()
                        || safeText(student.getClassName()).equalsIgnoreCase(selectedClass))
                .collect(Collectors.toList());

        Map<String, Long> totalStudentsByClassSection = students.stream()
                .collect(Collectors.groupingBy(student ->
                                safeText(student.getClassName()) + "||" + safeText(student.getSection()),
                        Collectors.counting()
                ));

        List<Attendance> attendanceList =
                attendanceRepository.findByAttendanceDateBetween(startDate, endDate).stream()
                        .filter(attendance -> attendance.getStudent() != null)
                        .filter(attendance -> selectedClass.isEmpty()
                                || safeText(attendance.getStudent().getClassName()).equalsIgnoreCase(selectedClass))
                        .collect(Collectors.toList());

        Map<String, List<Attendance>> attendanceByClassSection = attendanceList.stream()
                .collect(Collectors.groupingBy(attendance ->
                        safeText(attendance.getStudent().getClassName())
                                + "||"
                                + safeText(attendance.getStudent().getSection())
                ));

        Set<String> keys = new TreeSet<>();
        keys.addAll(totalStudentsByClassSection.keySet());
        keys.addAll(attendanceByClassSection.keySet());

        List<SectionAnalyticsDTO> result = new ArrayList<>();

        for (String key : keys) {
            String[] parts = key.split("\\|\\|", -1);
            String currentClassName = parts.length > 0 ? parts[0] : "";
            String currentSection = parts.length > 1 ? parts[1] : "";

            List<Attendance> records = attendanceByClassSection.getOrDefault(key, Collections.emptyList());

            long presentCount = records.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absentCount = records.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long totalMarked = presentCount + absentCount;

            double attendancePercentage =
                    totalMarked == 0
                            ? 0.0
                            : roundToTwoDecimals((presentCount * 100.0) / totalMarked);

            result.add(new SectionAnalyticsDTO(
                    currentClassName,
                    currentSection,
                    totalStudentsByClassSection.getOrDefault(key, 0L),
                    attendancePercentage,
                    0.0,
                    absentCount
            ));
        }

        result.sort(Comparator
                .comparing(SectionAnalyticsDTO::getClassName, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(SectionAnalyticsDTO::getSection, Comparator.nullsLast(String::compareToIgnoreCase)));

        return result;
    }

    public List<TeacherReplacementTrendDTO> getReplacementTrend(
            LocalDate startDate,
            LocalDate endDate
    ) {

        return new ArrayList<>();
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}