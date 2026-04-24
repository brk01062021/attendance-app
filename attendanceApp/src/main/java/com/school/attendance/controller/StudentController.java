package com.school.attendance.controller;

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
        // your existing logic
        return repository.save(student);
    }

    @GetMapping
    public List<Student> getStudents() {
        return repository.findAll();
    }

    @GetMapping("/filter")
    public List<Student> getStudentsByClassAndSection(
            @RequestParam String className,
            @RequestParam String section) {

        return repository.findByClassNameAndSection(className, section);
    }
    @PostMapping("/bulk")
    public List<Student> addStudents(@RequestBody List<Student> students) {
        List<Student> result = new ArrayList<>();

        for (Student student : students) {
            List<Student> existingStudents = repository.findByNameAndClassNameAndSection(
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