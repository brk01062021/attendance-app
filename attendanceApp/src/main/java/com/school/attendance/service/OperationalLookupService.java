package com.school.attendance.service;

import com.school.attendance.dto.StudentSearchDTO;
import com.school.attendance.dto.TeacherSearchDTO;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.TeacherAssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class OperationalLookupService {

    private final StudentRepository studentRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;

    public OperationalLookupService(StudentRepository studentRepository, TeacherAssignmentRepository teacherAssignmentRepository) {
        this.studentRepository = studentRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
    }

    public List<String> academicYears() {
        int current = Year.now().getValue();
        List<String> years = new ArrayList<>();
        years.add((current - 1) + "-" + current);
        years.add(current + "-" + (current + 1));
        years.add((current + 1) + "-" + (current + 2));
        return years;
    }

    public List<String> months(String schoolId) {
        return List.of("2026-05", "2026-04", "2026-03");
    }

    public List<String> classes(String schoolId) {
        return studentRepository.findDistinctClassNames().stream()
                .filter(v -> v != null && !v.isBlank())
                .toList();
    }

    public List<String> sections(String schoolId, String className) {
        if (className == null || className.isBlank()) {
            return studentRepository.findDistinctClassSectionLabels();
        }
        return studentRepository.findDistinctSectionsByClassName(className).stream()
                .filter(v -> v != null && !v.isBlank())
                .toList();
    }

    public List<String> subjects(String schoolId) {
        return teacherAssignmentRepository.findDistinctSubjects().stream()
                .filter(v -> v != null && !v.isBlank())
                .toList();
    }

    public List<StudentSearchDTO> students(String schoolId, String query) {
        return studentRepository.searchTenantStudents(normalizeQuery(query));
    }

    public List<TeacherSearchDTO> teachers(String schoolId, String query) {
        return teacherAssignmentRepository.searchTeachers(normalizeQuery(query));
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }
}
