package com.school.attendance.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private List<String> students = new ArrayList<>();

    @PostMapping
    public String addStudent(@RequestBody String name) {
        students.add(name);
        return "Student added: " + name;
    }

    @GetMapping
    public List<String> getStudents() {
        return students;
    }
}