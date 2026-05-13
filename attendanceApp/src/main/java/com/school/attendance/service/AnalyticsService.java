package com.school.attendance.service;

import com.school.attendance.dto.AnalyticsSummaryDTO;
import com.school.attendance.dto.AttendanceTrendDTO;
import com.school.attendance.dto.ClassAttendanceTrendDTO;
import com.school.attendance.dto.TeacherReplacementTrendDTO;
import com.school.attendance.entity.Attendance;
import com.school.attendance.entity.AttendanceStatus;
import com.school.attendance.entity.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.TeacherScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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