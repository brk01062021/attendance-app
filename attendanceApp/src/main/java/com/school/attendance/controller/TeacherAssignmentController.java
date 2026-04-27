package com.school.attendance.controller;

import com.school.attendance.entity.TeacherAssignment;
import com.school.attendance.repository.TeacherAssignmentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher-assignments")
@CrossOrigin("*")
public class TeacherAssignmentController {

    private final TeacherAssignmentRepository repository;

    public TeacherAssignmentController(TeacherAssignmentRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public TeacherAssignment addAssignment(@RequestBody TeacherAssignment assignment) {
        return repository.save(assignment);
    }

    @PostMapping("/bulk")
    public List<TeacherAssignment> addAssignments(@RequestBody List<TeacherAssignment> assignments) {
        return repository.saveAll(assignments);
    }

    @GetMapping
    public List<TeacherAssignment> getAllAssignments() {
        return repository.findAll();
    }

    @GetMapping("/subjects")
    public List<String> getSubjects(@RequestParam Long teacherId) {
        return repository.findSubjectsByTeacherId(teacherId);
    }

    @GetMapping("/classes")
    public List<String> getClasses(
            @RequestParam Long teacherId,
            @RequestParam String subjectName) {

        return repository.findClassesByTeacherIdAndSubjectName(teacherId, subjectName);
    }

    @GetMapping("/sections")
    public List<String> getSections(
            @RequestParam Long teacherId,
            @RequestParam String subjectName,
            @RequestParam String className) {

        return repository.findSectionsByTeacherIdSubjectNameAndClassName(
                teacherId,
                subjectName,
                className
        );
    }
}