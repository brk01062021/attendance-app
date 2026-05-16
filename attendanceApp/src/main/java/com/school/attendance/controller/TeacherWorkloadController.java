package com.school.attendance.controller;

import com.school.attendance.dto.TeacherFatigueAlertDTO;
import com.school.attendance.dto.TeacherReplacementLoadDTO;
import com.school.attendance.dto.TeacherWorkloadInsightDTO;
import com.school.attendance.service.TeacherWorkloadService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/teacher-workload")
@CrossOrigin(origins = "*")
public class TeacherWorkloadController {

    private final TeacherWorkloadService teacherWorkloadService;

    public TeacherWorkloadController(TeacherWorkloadService teacherWorkloadService) {
        this.teacherWorkloadService = teacherWorkloadService;
    }

    @GetMapping("/summary")
    public List<TeacherWorkloadInsightDTO> getSummary(@RequestParam(required = false) LocalDate date) {
        return teacherWorkloadService.getDailySummary(date);
    }

    @GetMapping("/fatigue-alerts")
    public List<TeacherFatigueAlertDTO> getFatigueAlerts(@RequestParam(required = false) LocalDate date) {
        return teacherWorkloadService.getFatigueAlerts(date);
    }

    @GetMapping("/teacher/{teacherId}")
    public TeacherWorkloadInsightDTO getTeacherInsight(@PathVariable Long teacherId, @RequestParam(required = false) LocalDate date) {
        return teacherWorkloadService.getTeacherDailyInsight(teacherId, date);
    }

    @GetMapping("/replacement-load")
    public List<TeacherReplacementLoadDTO> getReplacementLoad(@RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate) {
        return teacherWorkloadService.getReplacementLoad(fromDate, toDate);
    }
}
