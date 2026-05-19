package com.school.attendance.controller;

import com.school.attendance.dto.AcademicRuleDTO;
import com.school.attendance.dto.AcademicRulesSummaryDTO;
import com.school.attendance.dto.ClassTeacherPoolDTO;
import com.school.attendance.dto.TeacherWorkloadSummaryDTO;
import com.school.attendance.dto.TimetableConflictDTO;
import com.school.attendance.dto.TimetableGenerationRequestDTO;
import com.school.attendance.dto.TimetableGenerationResponseDTO;
import com.school.attendance.service.TimetableGenerationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/timetable")
@CrossOrigin(origins = "*")
public class TimetableGenerationController {

    private final TimetableGenerationService timetableGenerationService;

    public TimetableGenerationController(TimetableGenerationService timetableGenerationService) {
        this.timetableGenerationService = timetableGenerationService;
    }

    @PostMapping("/generate")
    public TimetableGenerationResponseDTO generate(@RequestBody TimetableGenerationRequestDTO request) {
        return timetableGenerationService.generate(request);
    }

    @GetMapping("/default-pools")
    public List<ClassTeacherPoolDTO> getDefaultPools() {
        return timetableGenerationService.getDefaultPools();
    }

    @PostMapping("/validate")
    public TimetableGenerationResponseDTO validate(@RequestBody TimetableGenerationRequestDTO request) {
        return timetableGenerationService.validate(request);
    }

    @GetMapping("/academic-rules/defaults")
    public List<AcademicRuleDTO> getDefaultAcademicRules() {
        return timetableGenerationService.getDefaultAcademicRules(List.of("1", "2"));
    }

    @PostMapping("/academic-rules/defaults")
    public List<AcademicRuleDTO> getDefaultAcademicRulesForClasses(@RequestBody TimetableGenerationRequestDTO request) {
        return timetableGenerationService.getDefaultAcademicRules(request.getClassNames());
    }

    @PostMapping("/academic-rules/validate")
    public AcademicRulesSummaryDTO validateAcademicRules(@RequestBody TimetableGenerationRequestDTO request) {
        return timetableGenerationService.validateAcademicRules(request.getAcademicRules(), request.getClassNames(), request.getSections());
    }

    @GetMapping("/review/{batchId}")
    public TimetableGenerationResponseDTO review(@PathVariable String batchId) {
        return timetableGenerationService.review(batchId);
    }

    @GetMapping("/conflicts/{batchId}")
    public List<TimetableConflictDTO> conflicts(@PathVariable String batchId) {
        return timetableGenerationService.conflicts(batchId);
    }

    @GetMapping("/workload-analysis/{batchId}")
    public List<TeacherWorkloadSummaryDTO> workloadAnalysis(@PathVariable String batchId) {
        return timetableGenerationService.workloadAnalysis(batchId);
    }
}
