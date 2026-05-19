package com.school.attendance.controller;

import com.school.attendance.dto.AcademicRuleDTO;
import com.school.attendance.dto.AcademicRulesSummaryDTO;
import com.school.attendance.dto.ClassTeacherPoolDTO;
import com.school.attendance.dto.TeacherWorkloadSummaryDTO;
import com.school.attendance.dto.TimetableConflictDTO;
import com.school.attendance.dto.TimetableGenerationRequestDTO;
import com.school.attendance.dto.TimetableGenerationResponseDTO;
import com.school.attendance.dto.PrincipalTimetableIntelligenceDTO;
import com.school.attendance.dto.TimetableExportResponseDTO;
import com.school.attendance.dto.TimetableManualEditRequestDTO;
import com.school.attendance.dto.TimetablePublishResponseDTO;
import com.school.attendance.dto.TimetableRepairResultDTO;
import com.school.attendance.dto.TimetablePublishAuditDTO;
import org.springframework.web.bind.annotation.RequestParam;
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
        return timetableGenerationService.validateAcademicRules(
                request.getAcademicRules(),
                request.getClassNames(),
                request.getSections()
        );
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

    @PostMapping({"/repair/{batchId}", "/auto-repair/{batchId}"})
    public TimetableRepairResultDTO repair(@PathVariable String batchId) {
        return timetableGenerationService.repair(batchId);
    }

    @PostMapping("/manual-edit/{batchId}")
    public TimetableGenerationResponseDTO manualEdit(
            @PathVariable String batchId,
            @RequestBody TimetableManualEditRequestDTO request
    ) {
        return timetableGenerationService.manualEdit(batchId, request);
    }

    @PostMapping("/publish/{batchId}")
    public TimetablePublishResponseDTO publishByPath(@PathVariable String batchId) {
        return timetableGenerationService.publish(batchId);
    }

    @GetMapping("/publish-history/{batchId}")
    public List<TimetablePublishAuditDTO> publishHistory(@PathVariable String batchId) {
        return timetableGenerationService.publishHistory(batchId);
    }

    @GetMapping("/latest-published")
    public TimetablePublishAuditDTO latestPublished() {
        return timetableGenerationService.latestPublished();
    }

    @PostMapping("/publish")
    public TimetablePublishResponseDTO publishByBody(@RequestBody java.util.Map<String, String> request) {
        return timetableGenerationService.publish(
                request == null ? null : request.get("generatedBatchId")
        );
    }

    @GetMapping("/export/{batchId}")
    public TimetableExportResponseDTO export(
            @PathVariable String batchId,
            @RequestParam(defaultValue = "EXCEL") String format
    ) {
        return timetableGenerationService.export(batchId, format);
    }

    @GetMapping("/principal-intelligence/{batchId}")
    public PrincipalTimetableIntelligenceDTO principalIntelligence(@PathVariable String batchId) {
        return timetableGenerationService.principalIntelligence(batchId);
    }
}