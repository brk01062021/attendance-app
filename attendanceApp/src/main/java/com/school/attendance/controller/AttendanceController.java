package com.school.attendance.controller;

import com.school.attendance.dto.AdminDashboardDTO;
import com.school.attendance.dto.AttendanceReportDTO;
import com.school.attendance.dto.ClassDashboardDTO;
import com.school.attendance.dto.TeacherWiseDashboardDTO;
import com.school.attendance.dto.SubjectDashboardDTO;
import com.school.attendance.dto.TeacherClassDashboardDTO;
import com.school.attendance.dto.DateRangeDashboardDTO;
import com.school.attendance.dto.AttendanceRequest;
import com.school.attendance.dto.BulkAttendanceRequest;
import com.school.attendance.dto.TeacherDashboardDTO;
import com.school.attendance.entity.Attendance;
import com.school.attendance.entity.AttendanceStatus;
import com.school.attendance.entity.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping
    public List<Attendance> getAllOrFilteredAttendance(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) LocalDate attendanceDate
    ) {
        if (teacherId != null
                && subjectName != null
                && className != null
                && section != null
                && attendanceDate != null) {

            return attendanceRepository
                    .findByTeacherIdAndSubjectNameAndClassNameAndSectionAndAttendanceDate(
                            teacherId,
                            subjectName,
                            className,
                            section,
                            attendanceDate
                    );
        }

        return attendanceRepository.findAll();
    }

    @GetMapping({"/dashboard", "/dashboard/admin"})
    public AdminDashboardDTO getAdminDashboard(@RequestParam String date) {
        LocalDate attendanceDate = LocalDate.parse(date);

        List<Attendance> records =
                attendanceRepository.findByAttendanceDate(attendanceDate);

        long totalStudents = studentRepository.count();

        long present = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long absent = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long late = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long attended = present + late;

        double percentage =
                totalStudents == 0 ? 0 :
                        ((double) attended / totalStudents) * 100;

        return new AdminDashboardDTO(
                date,
                totalStudents,
                present,
                absent,
                late,
                percentage
        );
    }

    @GetMapping("/dashboard/teacher")
    public TeacherDashboardDTO getTeacherDashboard(
            @RequestParam Long teacherId,
            @RequestParam String date
    ) {
        LocalDate attendanceDate = LocalDate.parse(date);

        List<Attendance> records =
                attendanceRepository.findByTeacherIdAndAttendanceDate(
                        teacherId,
                        attendanceDate
                );

        if (records.isEmpty()) {
            return new TeacherDashboardDTO(
                    teacherId,
                    "",
                    0,
                    0,
                    0,
                    0,
                    0.0
            );
        }

        String teacherName = records.get(0).getTeacherName();

        long totalStudents = records.size();

        long present = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long late = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long absent = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long attended = present + late;

        double percentage =
                totalStudents == 0 ? 0 :
                        ((double) attended / totalStudents) * 100;

        return new TeacherDashboardDTO(
                teacherId,
                teacherName,
                totalStudents,
                present,
                absent,
                late,
                percentage
        );
    }

    @GetMapping("/today/teacher")
    public List<Attendance> getTodayAttendanceForTeacher(
            @RequestParam Long teacherId
    ) {
        LocalDate today = LocalDate.now();

        return attendanceRepository.findByTeacherIdAndAttendanceDate(
                teacherId,
                today
        );
    }

    @GetMapping("/teacher/date")
    public List<Attendance> getAttendanceForTeacherByDate(
            @RequestParam Long teacherId,
            @RequestParam String date
    ) {
        return attendanceRepository.findByTeacherIdAndAttendanceDate(
                teacherId,
                LocalDate.parse(date)
        );
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
            @RequestParam String date
    ) {
        List<Attendance> list =
                attendanceRepository.findByAttendanceDate(LocalDate.parse(date));

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
                .orElseThrow(() -> new RuntimeException(
                        "Student not found with id: " + request.getStudentId()
                ));

        Attendance attendance = attendanceRepository
                .findByStudentIdAndAttendanceDateAndTeacherIdAndSubjectNameAndClassNameAndSection(
                        request.getStudentId(),
                        request.getAttendanceDate(),
                        request.getTeacherId(),
                        request.getSubjectName(),
                        request.getClassName(),
                        request.getSection()
                )
                .orElse(new Attendance());

        AttendanceStatus status;
        try {
            status = AttendanceStatus.valueOf(request.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status. Use PRESENT, ABSENT or LATE");
        }

        attendance.setStudent(student);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setTeacherId(request.getTeacherId());
        attendance.setTeacherName(request.getTeacherName());
        attendance.setSubjectName(request.getSubjectName());
        attendance.setClassName(request.getClassName());
        attendance.setSection(request.getSection());
        attendance.setStatus(status);

        return attendanceRepository.save(attendance);
    }

    @PostMapping("/bulk")
    public List<Attendance> markBulkAttendance(
            @RequestBody BulkAttendanceRequest request
    ) {
        List<Attendance> savedAttendance = new ArrayList<>();

        for (AttendanceRequest item : request.getAttendanceList()) {
            Student student = studentRepository.findById(item.getStudentId())
                    .orElseThrow(() -> new RuntimeException(
                            "Student not found with id: " + item.getStudentId()
                    ));

            Attendance attendance = attendanceRepository
                    .findByStudentIdAndAttendanceDateAndTeacherIdAndSubjectNameAndClassNameAndSection(
                            item.getStudentId(),
                            item.getAttendanceDate(),
                            item.getTeacherId(),
                            item.getSubjectName(),
                            item.getClassName(),
                            item.getSection()
                    )
                    .orElse(new Attendance());

            AttendanceStatus status = AttendanceStatus.valueOf(item.getStatus().toUpperCase());

            attendance.setStudent(student);
            attendance.setTeacherId(item.getTeacherId());
            attendance.setTeacherName(item.getTeacherName());
            attendance.setSubjectName(item.getSubjectName());
            attendance.setClassName(item.getClassName());
            attendance.setSection(item.getSection());
            attendance.setAttendanceDate(item.getAttendanceDate());
            attendance.setStatus(status);

            savedAttendance.add(attendanceRepository.save(attendance));
        }

        return savedAttendance;
    }

    @GetMapping("/student/{studentId}")
    public List<Attendance> getAttendanceByStudent(
            @PathVariable Long studentId
    ) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @GetMapping("/date")
    public List<Attendance> getAttendanceByDate(
            @RequestParam String date
    ) {
        return attendanceRepository.findByAttendanceDate(LocalDate.parse(date));
    }

    @GetMapping("/report")
    public List<AttendanceReportDTO> getAttendanceReport(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section
    ) {
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
            @RequestParam int month
    ) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Student> students =
                studentRepository.findByClassNameAndSection(className, section);

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

    @GetMapping("/dashboard/admin/classes")
    public List<ClassDashboardDTO> getAdminClassDashboard(
            @RequestParam String date
    ) {
        LocalDate attendanceDate = LocalDate.parse(date);

        List<Attendance> records =
                attendanceRepository.findByAttendanceDate(attendanceDate);

        Map<String, List<Attendance>> grouped = new HashMap<>();

        for (Attendance attendance : records) {
            String key = attendance.getClassName() + "-" + attendance.getSection();

            grouped.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(attendance);
        }

        List<ClassDashboardDTO> result = new ArrayList<>();

        for (List<Attendance> classRecords : grouped.values()) {
            String className = classRecords.get(0).getClassName();
            String section = classRecords.get(0).getSection();

            long totalRecords = classRecords.size();

            long present = classRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absent = classRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long late = classRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                    .count();

            long attended = present + late;

            double percentage =
                    totalRecords == 0 ? 0 :
                            ((double) attended / totalRecords) * 100;

            result.add(new ClassDashboardDTO(
                    className,
                    section,
                    totalRecords,
                    present,
                    absent,
                    late,
                    percentage
            ));
        }

        return result;
    }

    @GetMapping("/dashboard/admin/teachers")
    public List<TeacherWiseDashboardDTO> getAdminTeacherDashboard(
            @RequestParam String date
    ) {
        LocalDate attendanceDate = LocalDate.parse(date);

        List<Attendance> records =
                attendanceRepository.findByAttendanceDate(attendanceDate);

        Map<Long, List<Attendance>> grouped = new HashMap<>();

        for (Attendance attendance : records) {
            Long teacherId = attendance.getTeacherId();

            grouped.computeIfAbsent(teacherId, k -> new ArrayList<>())
                    .add(attendance);
        }

        List<TeacherWiseDashboardDTO> result = new ArrayList<>();

        for (List<Attendance> teacherRecords : grouped.values()) {
            Long teacherId = teacherRecords.get(0).getTeacherId();
            String teacherName = teacherRecords.get(0).getTeacherName();

            long totalRecords = teacherRecords.size();

            long present = teacherRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absent = teacherRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long late = teacherRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                    .count();

            long attended = present + late;

            double percentage =
                    totalRecords == 0 ? 0 :
                            ((double) attended / totalRecords) * 100;

            result.add(new TeacherWiseDashboardDTO(
                    teacherId,
                    teacherName,
                    totalRecords,
                    present,
                    absent,
                    late,
                    percentage
            ));
        }

        return result;
    }

    @GetMapping("/dashboard/admin/subjects")
    public List<SubjectDashboardDTO> getAdminSubjectDashboard(
            @RequestParam String date
    ) {
        LocalDate attendanceDate = LocalDate.parse(date);

        List<Attendance> records =
                attendanceRepository.findByAttendanceDate(attendanceDate);

        Map<String, List<Attendance>> grouped = new HashMap<>();

        for (Attendance attendance : records) {
            String subjectName = attendance.getSubjectName();

            grouped.computeIfAbsent(subjectName, k -> new ArrayList<>())
                    .add(attendance);
        }

        List<SubjectDashboardDTO> result = new ArrayList<>();

        for (List<Attendance> subjectRecords : grouped.values()) {
            String subjectName = subjectRecords.get(0).getSubjectName();

            long totalRecords = subjectRecords.size();

            long present = subjectRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absent = subjectRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long late = subjectRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                    .count();

            long attended = present + late;

            double percentage =
                    totalRecords == 0 ? 0 :
                            ((double) attended / totalRecords) * 100;

            result.add(new SubjectDashboardDTO(
                    subjectName,
                    totalRecords,
                    present,
                    absent,
                    late,
                    percentage
            ));
        }

        return result;
    }

    @GetMapping("/dashboard/admin/date-range")
    public List<DateRangeDashboardDTO> getAdminDateRangeDashboard(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<Attendance> records =
                attendanceRepository.findByAttendanceDateBetween(start, end);

        Map<LocalDate, List<Attendance>> grouped = new HashMap<>();

        for (Attendance attendance : records) {
            LocalDate date = attendance.getAttendanceDate();

            grouped.computeIfAbsent(date, k -> new ArrayList<>())
                    .add(attendance);
        }

        List<DateRangeDashboardDTO> result = new ArrayList<>();

        for (LocalDate date : grouped.keySet()) {
            List<Attendance> dateRecords = grouped.get(date);

            long totalRecords = dateRecords.size();

            long present = dateRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absent = dateRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long late = dateRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                    .count();

            long attended = present + late;

            double percentage =
                    totalRecords == 0 ? 0 :
                            ((double) attended / totalRecords) * 100;

            result.add(new DateRangeDashboardDTO(
                    date.toString(),
                    totalRecords,
                    present,
                    absent,
                    late,
                    percentage
            ));
        }

        result.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        return result;
    }

    @GetMapping("/dashboard/teacher/classes")
    public List<TeacherClassDashboardDTO> getTeacherClassDashboard(
            @RequestParam Long teacherId,
            @RequestParam String date
    ) {
        LocalDate attendanceDate = LocalDate.parse(date);

        List<Attendance> records =
                attendanceRepository.findByTeacherIdAndAttendanceDate(
                        teacherId,
                        attendanceDate
                );

        Map<String, List<Attendance>> grouped = new HashMap<>();

        for (Attendance attendance : records) {
            String key = attendance.getClassName()
                    + "-"
                    + attendance.getSection()
                    + "-"
                    + attendance.getSubjectName();

            grouped.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(attendance);
        }

        List<TeacherClassDashboardDTO> result = new ArrayList<>();

        for (List<Attendance> classRecords : grouped.values()) {
            String className = classRecords.get(0).getClassName();
            String section = classRecords.get(0).getSection();
            String subjectName = classRecords.get(0).getSubjectName();

            long totalStudents = classRecords.size();

            long present = classRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                    .count();

            long absent = classRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            long late = classRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                    .count();

            long attended = present + late;

            double percentage =
                    totalStudents == 0 ? 0 :
                            ((double) attended / totalStudents) * 100;

            result.add(new TeacherClassDashboardDTO(
                    className,
                    section,
                    subjectName,
                    totalStudents,
                    present,
                    absent,
                    late,
                    percentage
            ));
        }

        return result;
    }
}