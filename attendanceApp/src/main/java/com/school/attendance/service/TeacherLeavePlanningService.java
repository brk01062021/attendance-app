package com.school.attendance.service;

import com.school.attendance.dto.ReplacementRecommendationDTO;
import com.school.attendance.dto.TeacherLeaveRequestDTO;
import com.school.attendance.dto.TeacherWorkloadProtectionDTO;
import com.school.attendance.entity.Notification;
import com.school.attendance.entity.TeacherLeaveEnquiry;
import com.school.attendance.entity.TeacherSchedule;
import com.school.attendance.entity.TeacherScheduleStatus;
import com.school.attendance.repository.NotificationRepository;
import com.school.attendance.repository.TeacherLeaveEnquiryRepository;
import com.school.attendance.repository.TeacherScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherLeavePlanningService {

    private final TeacherScheduleRepository teacherScheduleRepository;
    private final TeacherLeaveEnquiryRepository teacherLeaveEnquiryRepository;
    private final NotificationRepository notificationRepository;

    public TeacherLeavePlanningService(
            TeacherScheduleRepository teacherScheduleRepository,
            TeacherLeaveEnquiryRepository teacherLeaveEnquiryRepository,
            NotificationRepository notificationRepository
    ) {
        this.teacherScheduleRepository = teacherScheduleRepository;
        this.teacherLeaveEnquiryRepository = teacherLeaveEnquiryRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<ReplacementRecommendationDTO> previewReplacements(TeacherLeaveRequestDTO request) {
        LocalDate from = LocalDate.parse(request.getFromDate());
        LocalDate to = LocalDate.parse(request.getToDate());
        List<TeacherSchedule> leavePeriods = teacherScheduleRepository
                .findByTeacherIdAndScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(request.getTeacherId(), from, to);

        return leavePeriods.stream()
                .map(this::buildBestRecommendation)
                .toList();
    }


    public Map<String, Object> submitLeaveEnquiry(TeacherLeaveRequestDTO request) {
        LocalDate from = LocalDate.parse(request.getFromDate());
        LocalDate to = LocalDate.parse(request.getToDate());
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("To date cannot be before from date");
        }

        TeacherLeaveEnquiry enquiry = new TeacherLeaveEnquiry();
        enquiry.setTeacherId(request.getTeacherId());
        enquiry.setTeacherName(request.getTeacherName());
        enquiry.setFromDate(from);
        enquiry.setToDate(to);
        enquiry.setLeaveType(request.getLeaveType() == null || request.getLeaveType().isBlank() ? "PLANNED_LEAVE" : request.getLeaveType());
        enquiry.setReason(request.getReason());
        enquiry.setStatus("PENDING");
        TeacherLeaveEnquiry saved = teacherLeaveEnquiryRepository.save(enquiry);

        createRoleNotification("ADMIN", saved);
        createRoleNotification("PRINCIPAL", saved);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Leave enquiry submitted successfully");
        response.put("enquiryId", saved.getId());
        response.put("status", saved.getStatus());
        response.put("requiresAdminApproval", true);
        response.put("notificationSentTo", List.of("ADMIN", "PRINCIPAL"));
        return response;
    }

    public List<TeacherLeaveEnquiry> teacherLeaveHistory(Long teacherId) {
        return teacherLeaveEnquiryRepository.findByTeacherIdOrderByRequestedAtDesc(teacherId);
    }

    public List<TeacherLeaveEnquiry> pendingLeaveEnquiries(String fromDate, String toDate) {
        if (fromDate == null || toDate == null || fromDate.isBlank() || toDate.isBlank()) {
            return teacherLeaveEnquiryRepository.findByStatusOrderByRequestedAtDesc("PENDING");
        }
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);
        return teacherLeaveEnquiryRepository.findByFromDateLessThanEqualAndToDateGreaterThanEqualOrderByRequestedAtDesc(to, from)
                .stream()
                .filter(item -> "PENDING".equalsIgnoreCase(item.getStatus()))
                .toList();
    }

    public Map<String, Object> approveLeaveEnquiry(Long enquiryId, String adminRemarks) {
        TeacherLeaveEnquiry enquiry = teacherLeaveEnquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Leave enquiry not found: " + enquiryId));

        TeacherScheduleStatus leaveStatus = "UNPLANNED_LEAVE".equalsIgnoreCase(enquiry.getLeaveType())
                ? TeacherScheduleStatus.UNPLANNED_LEAVE
                : TeacherScheduleStatus.PLANNED_LEAVE;

        List<TeacherSchedule> schedules = teacherScheduleRepository
                .findByTeacherIdAndScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(
                        enquiry.getTeacherId(), enquiry.getFromDate(), enquiry.getToDate());
        schedules.forEach(schedule -> schedule.setStatus(leaveStatus));
        teacherScheduleRepository.saveAll(schedules);

        enquiry.setStatus("APPROVED");
        enquiry.setAdminRemarks(adminRemarks);
        enquiry.setDecidedAt(java.time.LocalDateTime.now());
        teacherLeaveEnquiryRepository.save(enquiry);
        createTeacherDecisionNotification(enquiry, "APPROVED", adminRemarks);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Leave enquiry approved. Replacement workflow can now start.");
        response.put("enquiryId", enquiry.getId());
        response.put("affectedPeriods", schedules.size());
        response.put("status", enquiry.getStatus());
        response.put("scheduleStatus", leaveStatus.name());
        return response;
    }

    public Map<String, Object> rejectLeaveEnquiry(Long enquiryId, String adminRemarks) {
        TeacherLeaveEnquiry enquiry = teacherLeaveEnquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Leave enquiry not found: " + enquiryId));
        enquiry.setStatus("REJECTED");
        enquiry.setAdminRemarks(adminRemarks);
        enquiry.setDecidedAt(java.time.LocalDateTime.now());
        teacherLeaveEnquiryRepository.save(enquiry);
        createTeacherDecisionNotification(enquiry, "REJECTED", adminRemarks);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Leave enquiry rejected");
        response.put("enquiryId", enquiry.getId());
        response.put("status", enquiry.getStatus());
        return response;
    }

    private void createTeacherDecisionNotification(TeacherLeaveEnquiry enquiry, String status, String adminRemarks) {
        Notification notification = new Notification();
        notification.setUserId(enquiry.getTeacherId());
        notification.setRole("TEACHER");
        notification.setTitle("Leave Enquiry " + status);
        notification.setType("TEACHER_LEAVE_STATUS");
        String remarks = adminRemarks == null || adminRemarks.isBlank() ? "No remarks provided." : adminRemarks;
        notification.setMessage("Your leave enquiry from " + enquiry.getFromDate() + " to " + enquiry.getToDate()
                + " was " + status.toLowerCase() + ". Remarks: " + remarks);
        notificationRepository.save(notification);
    }

    private void createRoleNotification(String role, TeacherLeaveEnquiry enquiry) {
        Notification notification = new Notification();
        notification.setRole(role);
        notification.setTitle("Teacher Leave Enquiry");
        notification.setType("TEACHER_LEAVE_ENQUIRY");
        notification.setMessage((enquiry.getTeacherName() == null ? "Teacher" : enquiry.getTeacherName())
                + " requested leave from " + enquiry.getFromDate() + " to " + enquiry.getToDate()
                + ". Review it in Leave Approvals.");
        notificationRepository.save(notification);
    }

    public Map<String, Object> submitLeave(TeacherLeaveRequestDTO request) {
        LocalDate from = LocalDate.parse(request.getFromDate());
        LocalDate to = LocalDate.parse(request.getToDate());
        TeacherScheduleStatus status = "UNPLANNED_LEAVE".equalsIgnoreCase(request.getLeaveType())
                ? TeacherScheduleStatus.UNPLANNED_LEAVE
                : TeacherScheduleStatus.PLANNED_LEAVE;

        List<TeacherSchedule> schedules = teacherScheduleRepository
                .findByTeacherIdAndScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(request.getTeacherId(), from, to);
        schedules.forEach(schedule -> schedule.setStatus(status));
        teacherScheduleRepository.saveAll(schedules);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Leave submitted successfully");
        response.put("affectedPeriods", schedules.size());
        response.put("status", status.name());
        response.put("requiresAdminApproval", true);
        return response;
    }

    public List<TeacherSchedule> pendingApprovals(String fromDate, String toDate) {
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);
        return teacherScheduleRepository.findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(from, to)
                .stream()
                .filter(schedule -> schedule.getStatus() == TeacherScheduleStatus.PLANNED_LEAVE
                        || schedule.getStatus() == TeacherScheduleStatus.UNPLANNED_LEAVE)
                .toList();
    }

    public Map<String, Object> approveLeave(Long scheduleId, Long replacementTeacherId, String replacementTeacherName) {
        TeacherSchedule schedule = teacherScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));
        if (replacementTeacherId != null && replacementTeacherId > 0) {
            schedule.setReplacementTeacherId(replacementTeacherId);
            schedule.setReplacementTeacherName(replacementTeacherName);
            schedule.setReplacementClass(true);
            schedule.setStatus(TeacherScheduleStatus.REPLACED);
        }
        teacherScheduleRepository.save(schedule);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Leave approval updated");
        response.put("scheduleId", scheduleId);
        response.put("replacementTeacherId", replacementTeacherId);
        response.put("status", schedule.getStatus().name());
        return response;
    }

    public List<TeacherWorkloadProtectionDTO> workloadProtection(String fromDate, String toDate) {
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);
        List<TeacherSchedule> schedules = teacherScheduleRepository
                .findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAscTeacherNameAsc(from, to);

        Map<Long, List<TeacherSchedule>> byTeacher = schedules.stream()
                .filter(item -> item.getTeacherId() != null)
                .collect(Collectors.groupingBy(TeacherSchedule::getTeacherId));

        List<TeacherWorkloadProtectionDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<TeacherSchedule>> entry : byTeacher.entrySet()) {
            List<TeacherSchedule> items = entry.getValue();
            int replacementPeriods = (int) schedules.stream()
                    .filter(item -> Objects.equals(item.getReplacementTeacherId(), entry.getKey()))
                    .count();
            int scheduledPeriods = items.size();
            int fatigueScore = Math.min(100, scheduledPeriods * 7 + replacementPeriods * 12);

            TeacherWorkloadProtectionDTO dto = new TeacherWorkloadProtectionDTO();
            dto.setTeacherId(entry.getKey());
            dto.setTeacherName(items.get(0).getTeacherName());
            dto.setScheduledPeriods(scheduledPeriods);
            dto.setReplacementPeriods(replacementPeriods);
            dto.setFatigueScore(fatigueScore);
            dto.setRiskLevel(fatigueScore >= 80 ? "HIGH" : fatigueScore >= 55 ? "MEDIUM" : "LOW");
            dto.setRecommendation(fatigueScore >= 80
                    ? "Avoid assigning extra replacement periods this week."
                    : fatigueScore >= 55 ? "Monitor replacement load before approval." : "Safe for normal workload.");
            result.add(dto);
        }

        return result.stream()
                .sorted(Comparator.comparingInt(TeacherWorkloadProtectionDTO::getFatigueScore).reversed())
                .toList();
    }

    private ReplacementRecommendationDTO buildBestRecommendation(TeacherSchedule leavePeriod) {
        List<TeacherSchedule> sameSlot = teacherScheduleRepository
                .findByScheduleDateAndStartTimeAndEndTimeAndTeacherIdNot(
                        leavePeriod.getScheduleDate(), leavePeriod.getStartTime(), leavePeriod.getEndTime(), leavePeriod.getTeacherId());

        Optional<TeacherSchedule> best = sameSlot.stream()
                .filter(candidate -> candidate.getStatus() == TeacherScheduleStatus.AVAILABLE)
                .min(Comparator.comparingInt(candidate -> dailyWorkload(candidate.getTeacherId(), candidate.getScheduleDate())));

        ReplacementRecommendationDTO dto = new ReplacementRecommendationDTO();
        dto.setScheduleId(leavePeriod.getId());
        dto.setTeacherId(leavePeriod.getTeacherId());
        dto.setTeacherName(leavePeriod.getTeacherName());
        dto.setClassName(leavePeriod.getClassName());
        dto.setSection(leavePeriod.getSection());
        dto.setSubjectName(leavePeriod.getSubjectName());
        dto.setScheduleDate(String.valueOf(leavePeriod.getScheduleDate()));
        dto.setPeriodTime(leavePeriod.getStartTime() + " - " + leavePeriod.getEndTime());

        if (best.isPresent()) {
            TeacherSchedule candidate = best.get();
            int workload = dailyWorkload(candidate.getTeacherId(), candidate.getScheduleDate());
            dto.setReplacementTeacherId(candidate.getTeacherId());
            dto.setReplacementTeacherName(candidate.getTeacherName());
            dto.setDailyWorkload(workload);
            dto.setOverloaded(workload >= 6);
            boolean sameClass = Objects.equals(candidate.getClassName(), leavePeriod.getClassName());
            boolean sameSection = Objects.equals(candidate.getSection(), leavePeriod.getSection());
            dto.setMatchType(sameClass && sameSection ? "SAME_CLASS_SECTION" : sameClass ? "SAME_CLASS" : "AVAILABLE_TEACHER");
            dto.setConfidenceScore(Math.max(40, 95 - workload * 8 - (sameClass ? 0 : 10) - (sameSection ? 0 : 5)));
        } else {
            dto.setReplacementTeacherName("No free teacher found");
            dto.setMatchType("NO_MATCH");
            dto.setConfidenceScore(0);
            dto.setOverloaded(false);
        }
        return dto;
    }

    private int dailyWorkload(Long teacherId, LocalDate date) {
        return teacherScheduleRepository.findByTeacherIdAndScheduleDate(teacherId, date).size();
    }
}
