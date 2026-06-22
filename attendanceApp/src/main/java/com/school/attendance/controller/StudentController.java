package com.school.attendance.controller;

import com.school.attendance.dto.StudentSearchDTO;
import com.school.attendance.entity.Student;
import com.school.attendance.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository repository;

    public StudentController(StudentRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Student addStudent(@RequestBody Student student) {
        return repository.save(student);
    }

    @GetMapping
    public List<Student> getStudents(
            @RequestParam String className,
            @RequestParam String section) {

        List<Student> exact = repository.findByClassNameAndSection(className, section);
        if (!exact.isEmpty()) {
            return exact;
        }
        return repository.findByClassAndSectionFlexible(className, section);
    }

    @GetMapping("/classes")
    public List<String> getClasses() {
        return repository.findDistinctClassNames();
    }

    @GetMapping("/sections")
    public List<String> getSectionsByClassName(@RequestParam String className) {
        return repository.findDistinctSectionsByClassName(className);
    }


    @GetMapping("/search")
    public List<StudentSearchDTO> searchStudents(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam(required = false, defaultValue = "") String query
    ) {
        return repository.searchStudentsForReport(
                className,
                section,
                query == null ? "" : query.trim()
        );
    }

    @PostMapping("/bulk")
    public List<Student> addStudents(@RequestBody List<Student> students) {
        List<Student> result = new ArrayList<>();

        for (Student student : students) {
            List<Student> existingStudents =
                    repository.findByNameAndClassNameAndSection(
                            student.getName(),
                            student.getClassName(),
                            student.getSection()
                    );

            if (!existingStudents.isEmpty()) {
                result.add(existingStudents.get(0));
            } else {
                result.add(repository.save(student));
            }
        }

        return result;
    }
}