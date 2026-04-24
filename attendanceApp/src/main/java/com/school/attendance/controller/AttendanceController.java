package com.school.attendance.controller;

import com.school.attendance.dto.AttendanceRequest;
import com.school.attendance.dto.BulkAttendanceRequest;
import com.school.attendance.entity.Attendance;
import com.school.attendance.entity.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;
import com.school.attendance.dto.AttendanceReportDTO;
import com.school.attendance.entity.AttendanceStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceController(AttendanceRepository attendanceRepository,
                                StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/summary")
    public Map<String, Long> getAttendanceSummary() {

        List<Attendance> all = attendanceRepository.findAll();

        long present = all.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absent = all.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long late = all.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        Map<String, Long> summary = new HashMap<>();
        summary.put("present", present);
        summary.put("absent", absent);
        summary.put("late", late);

        return summary;
    }

    @GetMapping("/summary/date")
    public Map<String, Long> getAttendanceSummaryByDate(
            @RequestParam String date) {

        List<Attendance> list = attendanceRepository.findByAttendanceDate(
                java.time.LocalDate.parse(date)
        );

        long present = list.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absent = list.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long late = list.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        Map<String, Long> summary = new HashMap<>();
        summary.put("present", present);
        summary.put("absent", absent);
        summary.put("late", late);

        return summary;
    }

    @PostMapping
    public Attendance markAttendance(@RequestBody AttendanceRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + request.getStudentId()));

        Attendance attendance = attendanceRepository
                .findByStudentIdAndAttendanceDate(request.getStudentId(), request.getAttendanceDate())
                .orElse(new Attendance());

        attendance.setStudent(student);
        attendance.setAttendanceDate(request.getAttendanceDate());
        AttendanceStatus status;
        try {
            status = AttendanceStatus.valueOf(request.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status. Use PRESENT, ABSENT or LATE");
        }

        attendance.setStatus(status);

        return attendanceRepository.save(attendance);
    }

    @PostMapping("/bulk")
    public List<Attendance> markBulkAttendance(@RequestBody BulkAttendanceRequest request) {
        List<Attendance> savedAttendance = new ArrayList<>();

        for (AttendanceRequest item : request.getAttendanceList()) {
            Student student = studentRepository.findById(item.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found with id: " + item.getStudentId()));

            Attendance attendance = attendanceRepository
                    .findByStudentIdAndAttendanceDate(item.getStudentId(), item.getAttendanceDate())
                    .orElse(new Attendance());

            AttendanceStatus status = AttendanceStatus.valueOf(item.getStatus().toUpperCase());

            attendance.setStudent(student);
            attendance.setAttendanceDate(item.getAttendanceDate());
            attendance.setStatus(status);

            savedAttendance.add(attendanceRepository.save(attendance));
        }

        return savedAttendance;
    }

    @GetMapping
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    @GetMapping("/student/{studentId}")
    public List<Attendance> getAttendanceByStudent(@PathVariable Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @GetMapping("/date")
    public List<Attendance> getAttendanceByDate(
            @RequestParam String date) {

        return attendanceRepository.findByAttendanceDate(
                java.time.LocalDate.parse(date)
        );
    }

    @GetMapping("/report")
    public List<AttendanceReportDTO> getAttendanceReport(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section) {

        List<Student> students;

        if (className != null && section != null) {
            students = studentRepository.findByClassNameAndSection(className, section);
        } else {
            students = studentRepository.findAll();
        }

        List<AttendanceReportDTO> report = new ArrayList<>();

        for (Student student : students) {
            List<Attendance> attendanceList =
                    attendanceRepository.findByStudentId(student.getId());

            long totalDays = attendanceList.size();

            long presentDays = attendanceList.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT
                            || a.getStatus() == AttendanceStatus.LATE)
                    .count();

            long absentDays = attendanceList.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            report.add(new AttendanceReportDTO(
                    student.getId(),
                    student.getName(),
                    student.getClassName(),
                    student.getSection(),
                    totalDays,
                    presentDays,
                    absentDays
            ));
        }

        return report;
    }
    @GetMapping("/report/monthly")
    public List<AttendanceReportDTO> getMonthlyAttendanceReport(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam int year,
            @RequestParam int month) {

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Student> students = studentRepository.findByClassNameAndSection(className, section);
        List<AttendanceReportDTO> report = new ArrayList<>();

        for (Student student : students) {
            List<Attendance> attendanceList =
                    attendanceRepository.findByStudentIdAndAttendanceDateBetween(
                            student.getId(),
                            startDate,
                            endDate
                    );

            long totalDays = attendanceList.size();

            long presentDays = attendanceList.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT
                            || a.getStatus() == AttendanceStatus.LATE)
                    .count();

            long absentDays = attendanceList.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            report.add(new AttendanceReportDTO(
                    student.getId(),
                    student.getName(),
                    student.getClassName(),
                    student.getSection(),
                    totalDays,
                    presentDays,
                    absentDays
            ));
        }

        return report;
    }
}