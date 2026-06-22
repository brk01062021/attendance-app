package com.school.attendance.controller;

import com.school.attendance.entity.Student;
import com.school.attendance.entity.TeacherExamResult;
import com.school.attendance.entity.TeacherSchedule;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.TeacherExamResultRepository;
import com.school.attendance.repository.TeacherScheduleRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher-results")
public class TeacherExamResultController {

    private final TeacherScheduleRepository teacherScheduleRepository;
    private final StudentRepository studentRepository;
    private final TeacherExamResultRepository teacherExamResultRepository;

    public TeacherExamResultController(TeacherScheduleRepository teacherScheduleRepository,
                                       StudentRepository studentRepository,
                                       TeacherExamResultRepository teacherExamResultRepository) {
        this.teacherScheduleRepository = teacherScheduleRepository;
        this.studentRepository = studentRepository;
        this.teacherExamResultRepository = teacherExamResultRepository;
    }

    @GetMapping("/history")
    public List<TeacherExamResult> history(@RequestHeader(value = "X-School-Id", required = false) String headerSchoolId,
                                           @RequestParam(required = false) String schoolId,
                                           @RequestParam Long teacherId) {
        return teacherExamResultRepository.findBySchoolIdIgnoreCaseAndTeacherIdOrderBySavedAtDesc(effectiveSchoolId(headerSchoolId, schoolId), teacherId);
    }

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestHeader(value = "X-School-Id", required = false) String headerSchoolId,
                                      @RequestParam(required = false) String schoolId,
                                      @RequestBody ResultUploadRequest request) {
        String safeSchoolId = effectiveSchoolId(headerSchoolId, schoolId);
        ValidationResult validation = validateRequest(safeSchoolId, request);
        if (!validation.issues.isEmpty()) {
            return Map.of(
                    "success", false,
                    "saved", 0,
                    "issues", validation.issues,
                    "message", "Fix validation issues before saving results."
            );
        }

        String targetStatus = Boolean.TRUE.equals(request.finalSubmit) ? "SUBMITTED" : "DRAFT";
        LocalDateTime now = LocalDateTime.now();
        List<TeacherExamResult> saved = new ArrayList<>();
        for (ValidatedMark row : validation.rows) {
            TeacherExamResult result = teacherExamResultRepository
                    .findBySchoolIdIgnoreCaseAndTeacherIdAndClassNameIgnoreCaseAndSectionIgnoreCaseAndSubjectNameIgnoreCaseAndExamNameIgnoreCaseAndStudentId(
                            safeSchoolId,
                            request.teacherId,
                            request.className,
                            request.section,
                            request.subjectName,
                            request.examName,
                            row.student.getId()
                    )
                    .orElse(new TeacherExamResult());

            if ("SUBMITTED".equalsIgnoreCase(result.getStatus())) {
                validation.issues.add("Results are already finally submitted for student " + studentIdentifier(row.student) + ".");
                continue;
            }

            result.setSchoolId(safeSchoolId);
            result.setTeacherId(request.teacherId);
            result.setTeacherName(blank(request.teacherName) ? validation.teacherName : request.teacherName);
            result.setClassName(request.className);
            result.setSection(request.section);
            result.setSubjectName(request.subjectName);
            result.setExamName(request.examName);
            result.setMaxMarks(request.maxMarks);
            result.setStudentId(row.student.getId());
            result.setStudentName(row.student.getName());
            result.setStudentIdentifier(studentIdentifier(row.student));
            result.setMarks(row.marks);
            result.setStatus(targetStatus);
            result.setSavedAt(now);
            if ("SUBMITTED".equals(targetStatus)) result.setSubmittedAt(now);
            saved.add(teacherExamResultRepository.save(result));
        }

        if (!validation.issues.isEmpty()) {
            return Map.of("success", false, "saved", saved.size(), "issues", validation.issues, "message", "Some rows could not be saved because submitted results are locked.");
        }

        return Map.of(
                "success", true,
                "status", targetStatus,
                "saved", saved.size(),
                "expectedStudents", validation.expectedStudents,
                "message", targetStatus.equals("SUBMITTED") ? "Results submitted and locked." : "Result draft saved."
        );
    }

    private ValidationResult validateRequest(String schoolId, ResultUploadRequest request) {
        ValidationResult result = new ValidationResult();
        if (request == null) {
            result.issues.add("Request body is required.");
            return result;
        }
        if (request.teacherId == null) result.issues.add("teacherId is required.");
        if (blank(request.className)) result.issues.add("className is required.");
        if (blank(request.section)) result.issues.add("section is required.");
        if (blank(request.subjectName)) result.issues.add("subjectName is required.");
        if (blank(request.examName)) result.issues.add("examName is required.");
        if (request.maxMarks == null || request.maxMarks <= 0) result.issues.add("maxMarks must be greater than 0.");
        if (request.rows == null || request.rows.isEmpty()) result.issues.add("At least one marks row is required.");
        if (!result.issues.isEmpty()) return result;

        List<TeacherSchedule> assigned = teacherScheduleRepository.findBySchoolIdIgnoreCaseAndActiveTimetableTrueOrderByScheduleDateAscStartTimeAscTeacherNameAsc(schoolId)
                .stream()
                .filter(s -> request.teacherId.equals(s.getTeacherId())
                        && equalsIgnoreCase(s.getClassName(), request.className)
                        && equalsIgnoreCase(s.getSection(), request.section)
                        && equalsIgnoreCase(s.getSubjectName(), request.subjectName))
                .toList();
        if (assigned.isEmpty()) {
            result.issues.add("Teacher is not assigned to this class, section and subject in the active published timetable.");
            return result;
        }
        result.teacherName = assigned.get(0).getTeacherName();

        List<Student> students = studentRepository.findByClassNameAndSection(request.className, request.section);
        result.expectedStudents = students.size();
        Map<String, Student> lookup = new HashMap<>();
        for (Student student : students) {
            addLookup(lookup, String.valueOf(student.getId()), student);
            addLookup(lookup, student.getAdmissionNumber(), student);
            addLookup(lookup, student.getRollNumber(), student);
        }

        Set<Long> seen = new HashSet<>();
        for (ResultMarkRow row : request.rows) {
            String inputId = row == null ? null : row.studentId;
            Student student = lookup.get(normalize(inputId));
            if (student == null) {
                result.issues.add("Student " + inputId + " is not in selected class/section.");
                continue;
            }
            if (!seen.add(student.getId())) {
                result.issues.add("Duplicate marks row for student " + studentIdentifier(student) + ".");
                continue;
            }
            if (row.marks == null || row.marks < 0 || row.marks > request.maxMarks) {
                result.issues.add("Marks for student " + studentIdentifier(student) + " must be between 0 and " + request.maxMarks + ".");
                continue;
            }
            result.rows.add(new ValidatedMark(student, row.marks));
        }

        Set<Long> expected = students.stream().map(Student::getId).collect(Collectors.toSet());
        expected.removeAll(seen);
        if (!expected.isEmpty()) {
            result.issues.add("Missing marks for " + expected.size() + " student(s). Upload whole-class results before final submission.");
        }
        return result;
    }

    private String effectiveSchoolId(String headerSchoolId, String schoolId) {
        String value = !blank(schoolId) ? schoolId : headerSchoolId;
        return blank(value) ? "DEMO" : value.trim().toUpperCase();
    }

    private static void addLookup(Map<String, Student> lookup, String key, Student student) {
        if (!blank(key)) lookup.put(normalize(key), student);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String studentIdentifier(Student student) {
        if (!blank(student.getAdmissionNumber())) return student.getAdmissionNumber();
        if (!blank(student.getRollNumber())) return student.getRollNumber();
        return String.valueOf(student.getId());
    }

    public static class ResultUploadRequest {
        public Long teacherId;
        public String teacherName;
        public String className;
        public String section;
        public String subjectName;
        public String examName;
        public Integer maxMarks;
        public Boolean finalSubmit;
        public List<ResultMarkRow> rows;
    }

    public static class ResultMarkRow {
        public String studentId;
        public Double marks;
    }

    private static class ValidationResult {
        List<String> issues = new ArrayList<>();
        List<ValidatedMark> rows = new ArrayList<>();
        int expectedStudents;
        String teacherName;
    }

    private record ValidatedMark(Student student, Double marks) {}
}
