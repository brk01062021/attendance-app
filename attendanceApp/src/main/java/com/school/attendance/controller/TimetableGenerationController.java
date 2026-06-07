package com.school.attendance.controller;

import com.school.attendance.dto.AcademicRuleDTO;
import com.school.attendance.dto.AcademicRulesSummaryDTO;
import com.school.attendance.dto.ClassTeacherPoolDTO;
import com.school.attendance.dto.ExistingTimetableImportResponseDTO;
import com.school.attendance.dto.ExistingTimetableImportStatusDTO;
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
import com.school.attendance.dto.TimetableBatchSummaryDTO;
import com.school.attendance.dto.TimetableArchiveSummaryDTO;
import com.school.attendance.dto.TimetableBinaryExportDTO;
import com.school.attendance.dto.TimetableLiveResponseDTO;
import com.school.attendance.dto.TimetableNotificationDTO;
import com.school.attendance.dto.TimetableVersionDTO;
import com.school.attendance.dto.TimetableRolloutReadinessDTO;
import org.springframework.web.bind.annotation.RequestParam;
import com.school.attendance.service.TimetableGenerationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    @PostMapping({"/repair/{batchId}", "/auto-repair/{batchId}", "/operations/repair/{batchId}", "/operations/auto-repair/{batchId}"})
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
    public TimetablePublishResponseDTO publishByPath(
            @PathVariable String batchId,
            @RequestParam(required = false) String approvedBy
    ) {
        return timetableGenerationService.publish(batchId, approvedBy);
    }

    @GetMapping("/publish-history/{batchId}")
    public List<TimetablePublishAuditDTO> publishHistory(@PathVariable String batchId) {
        return timetableGenerationService.publishHistory(batchId);
    }

    @GetMapping("/latest-published")
    public TimetablePublishAuditDTO latestPublished() {
        return timetableGenerationService.latestPublished();
    }

    @GetMapping("/batches")
    public List<TimetableBatchSummaryDTO> batches() {
        return timetableGenerationService.listBatches();
    }

    @GetMapping("/batch-summary/{batchId}")
    public TimetableBatchSummaryDTO batchSummary(@PathVariable String batchId) {
        return timetableGenerationService.batchSummary(batchId);
    }

    @PostMapping("/publish")
    public TimetablePublishResponseDTO publishByBody(@RequestBody java.util.Map<String, String> request) {
        return timetableGenerationService.publish(
                request == null ? null : request.get("generatedBatchId"),
                request == null ? null : request.get("approvedBy")
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

    @GetMapping({"/day18/live", "/operations/live", "/visibility/live"})
    public TimetableLiveResponseDTO liveTimetable(
            @RequestParam(required = false) String batchId,
            @RequestParam(defaultValue = "ADMIN") String role,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestHeader(value = "X-School-Id", required = false) String schoolId
    ) {
        return timetableGenerationService.liveTimetable(batchId, role, teacherId, teacherName, className, section);
    }

    @GetMapping("/live/teacher")
    public TimetableLiveResponseDTO liveTeacherTimetable(
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String teacherName,
            @RequestHeader(value = "X-School-Id", required = false) String schoolId
    ) {
        return timetableGenerationService.liveTimetable(batchId, "TEACHER", teacherId, teacherName, null, null);
    }

    @GetMapping("/live/student")
    public TimetableLiveResponseDTO liveStudentTimetable(
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestHeader(value = "X-School-Id", required = false) String schoolId
    ) {
        return timetableGenerationService.liveTimetable(batchId, "STUDENT", null, null, className, section);
    }

    @GetMapping("/live/parent")
    public TimetableLiveResponseDTO liveParentTimetable(
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestHeader(value = "X-School-Id", required = false) String schoolId
    ) {
        return timetableGenerationService.liveTimetable(batchId, "PARENT", null, null, className, section);
    }

    @PostMapping({"/operations/publish/{batchId}", "/visibility/publish/{batchId}"})
    public TimetablePublishResponseDTO publishOperationsPath(
            @PathVariable String batchId,
            @RequestParam(defaultValue = "ADMIN") String role,
            @RequestParam(required = false) String approvedBy
    ) {
        return timetableGenerationService.publishLock(batchId, role, approvedBy);
    }

    @PostMapping({"/day18/publish-lock/{batchId}", "/operations/publish-lock/{batchId}"})
    public TimetablePublishResponseDTO publishLock(
            @PathVariable String batchId,
            @RequestParam(defaultValue = "ADMIN") String role,
            @RequestParam(required = false) String approvedBy
    ) {
        return timetableGenerationService.publishLock(batchId, role, approvedBy);
    }

    @PostMapping({"/day18/swap/{batchId}", "/operations/swap/{batchId}"})
    public TimetableGenerationResponseDTO swapTimetableEntry(
            @PathVariable String batchId,
            @RequestParam(defaultValue = "ADMIN") String role,
            @RequestBody TimetableManualEditRequestDTO request
    ) {
        return timetableGenerationService.swapTimetableEntry(batchId, request, role);
    }

    @GetMapping({"/day18/export/{batchId}", "/operations/export/{batchId}"})
    public TimetableBinaryExportDTO binaryExport(
            @PathVariable String batchId,
            @RequestParam(defaultValue = "EXCEL") String format
    ) {
        return timetableGenerationService.binaryExport(batchId, format);
    }

    @GetMapping({"/day18/versions/{batchId}", "/operations/versions/{batchId}"})
    public List<TimetableVersionDTO> versions(@PathVariable String batchId) {
        return timetableGenerationService.versions(batchId);
    }

    @PostMapping({"/day18/rollback/{batchId}/{versionNumber}", "/operations/rollback/{batchId}/{versionNumber}"})
    public TimetableVersionDTO rollback(
            @PathVariable String batchId,
            @PathVariable Integer versionNumber,
            @RequestParam(defaultValue = "ADMIN") String role
    ) {
        return timetableGenerationService.rollback(batchId, versionNumber, role);
    }

    @GetMapping({"/day18/notifications/{batchId}", "/operations/notifications/{batchId}"})
    public List<TimetableNotificationDTO> notifications(@PathVariable String batchId) {
        return timetableGenerationService.notifications(batchId);
    }

    @GetMapping({"/day18/archives", "/operations/archives"})
    public List<TimetableArchiveSummaryDTO> archives() {
        return timetableGenerationService.archives();
    }

    @PostMapping({"/import-existing/preview", "/operations/import-existing"})
    public ExistingTimetableImportResponseDTO importExistingTimetablePreview(
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-School-Id", required = false) String headerSchoolId,
            @RequestParam(required = false) String schoolId,
            @RequestParam(required = false) String uploadedBy,
            @RequestParam(defaultValue = "false") boolean publish,
            @RequestParam(defaultValue = "ADMIN") String role,
            @RequestParam(required = false) String approvedBy
    ) {
        String effectiveSchoolId = schoolId != null && !schoolId.isBlank() ? schoolId : headerSchoolId;
        ExistingTimetableImportResponseDTO response = timetableGenerationService.importExistingTimetable(file, effectiveSchoolId, uploadedBy);
        if (publish && Boolean.TRUE.equals(response.getCanPublish())) {
            return timetableGenerationService.publishImportedTimetable(response.getImportBatchId(), role, approvedBy);
        }
        return response;
    }

    @GetMapping({"/import-existing/status", "/operations/import-existing/status"})
    public ExistingTimetableImportStatusDTO existingTimetableImportStatus(
            @RequestHeader(value = "X-School-Id", required = false) String headerSchoolId,
            @RequestParam(required = false) String schoolId
    ) {
        String effectiveSchoolId = schoolId != null && !schoolId.isBlank() ? schoolId : headerSchoolId;
        return timetableGenerationService.existingTimetableImportStatus(effectiveSchoolId);
    }

    @PostMapping("/import-existing/publish/{importBatchId}")
    public ExistingTimetableImportResponseDTO publishImportedTimetable(
            @PathVariable String importBatchId,
            @RequestParam(defaultValue = "ADMIN") String role,
            @RequestParam(required = false) String approvedBy
    ) {
        return timetableGenerationService.publishImportedTimetable(importBatchId, role, approvedBy);
    }

    @GetMapping({"/role-notifications", "/visibility/role-notifications"})
    public List<TimetableNotificationDTO> roleNotifications(
            @RequestParam(defaultValue = "TEACHER") String role,
            @RequestHeader(value = "X-School-Id", required = false) String schoolId
    ) {
        return timetableGenerationService.roleNotifications(role);
    }


    @GetMapping({"/day20/rollout-readiness/{batchId}", "/operations/rollout-readiness/{batchId}"})
    public TimetableRolloutReadinessDTO rolloutReadiness(@PathVariable String batchId) {
        return timetableGenerationService.rolloutReadiness(batchId);
    }

    @GetMapping({"/day18/principal-analytics", "/operations/principal-analytics"})
    public PrincipalTimetableIntelligenceDTO day18PrincipalAnalytics(@RequestParam String batchId) {
        return timetableGenerationService.principalIntelligence(batchId);
    }

    @GetMapping({"/day18/status/{batchId}", "/operations/status/{batchId}"})
    public Map<String, Object> day18Status(@PathVariable String batchId) {
        return timetableGenerationService.day18Status(batchId);
    }

}