package com.school.attendance.service;

import com.school.attendance.dto.StudentRiskDTO;
import com.school.attendance.entity.Attendance;
import com.school.attendance.entity.AttendanceStatus;
import com.school.attendance.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentRiskService {

    private final AttendanceRepository attendanceRepository;

    public StudentRiskService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public List<StudentRiskDTO> getRiskStudents(String fromDate, String toDate, String className, String section) {
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);
        List<Attendance> attendance = attendanceRepository.findByAttendanceDateBetween(from, to).stream()
                .filter(item -> className == null || className.isBlank() || className.equalsIgnoreCase(item.getClassName()))
                .filter(item -> section == null || section.isBlank() || section.equalsIgnoreCase(item.getSection()))
                .toList();

        Map<Long, List<Attendance>> byStudent = attendance.stream()
                .filter(item -> item.getStudent() != null && item.getStudent().getId() != null)
                .collect(Collectors.groupingBy(item -> item.getStudent().getId()));

        List<StudentRiskDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Attendance>> entry : byStudent.entrySet()) {
            List<Attendance> records = entry.getValue();
            long total = records.size();
            long absent = records.stream().filter(item -> item.getStatus() == AttendanceStatus.ABSENT).count();
            double percentage = total == 0 ? 0 : ((double) (total - absent) / total) * 100;
            if (percentage >= 90 && absent <= 1) {
                continue;
            }
            Attendance sample = records.get(0);
            StudentRiskDTO dto = new StudentRiskDTO();
            dto.setStudentId(entry.getKey());
            dto.setStudentName(sample.getStudent().getName());
            dto.setClassName(sample.getClassName());
            dto.setSection(sample.getSection());
            dto.setTotalClasses(total);
            dto.setAbsentCount(absent);
            dto.setAttendancePercentage(Math.round(percentage * 100.0) / 100.0);
            dto.setRiskLevel(percentage < 75 ? "HIGH" : percentage < 85 ? "MEDIUM" : "LOW");
            dto.setActionRequired(percentage < 75 ? "Parent follow-up required" : percentage < 85 ? "Teacher counselling recommended" : "Monitor trend");
            result.add(dto);
        }

        return result.stream()
                .sorted(Comparator.comparingDouble(StudentRiskDTO::getAttendancePercentage))
                .toList();
    }
}
