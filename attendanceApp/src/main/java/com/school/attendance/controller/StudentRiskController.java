package com.school.attendance.controller;

import com.school.attendance.dto.StudentRiskDTO;
import com.school.attendance.service.StudentRiskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student-risk")
public class StudentRiskController {

    private final StudentRiskService studentRiskService;

    public StudentRiskController(StudentRiskService studentRiskService) {
        this.studentRiskService = studentRiskService;
    }

    @GetMapping("/attendance")
    public List<StudentRiskDTO> attendanceRisk(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section
    ) {
        return studentRiskService.getRiskStudents(fromDate, toDate, className, section);
    }
}
