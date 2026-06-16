package com.school.attendance.service;

import com.school.attendance.dto.ActivityApprovalRequest;
import com.school.attendance.dto.ActivityResponse;
import com.school.attendance.dto.CreateActivityRequest;
import com.school.attendance.dto.UpdateActivityRequest;
import com.school.attendance.entity.Activity;
import com.school.attendance.entity.ActivityApprovalHistory;
import com.school.attendance.entity.ActivityApprovalStatus;
import com.school.attendance.entity.ActivityVisibilityType;
import com.school.attendance.repository.ActivityApprovalHistoryRepository;
import com.school.attendance.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityApprovalHistoryRepository approvalHistoryRepository;

    public ActivityService(ActivityRepository activityRepository,
                           ActivityApprovalHistoryRepository approvalHistoryRepository) {
        this.activityRepository = activityRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
    }

    @Transactional
    public ActivityResponse create(String schoolId, CreateActivityRequest request, boolean teacherDraft) {
        Activity activity = new Activity();
        activity.setSchoolId(normalizeSchoolId(schoolId));
        activity.setTitle(required(request.getTitle(), "Activity title is required"));
        activity.setDescription(request.getDescription());
        activity.setActivityDate(request.getActivityDate() == null ? LocalDate.now() : request.getActivityDate());
        activity.setCreatedBy(request.getCreatedBy());
        activity.setVisibilityType(parseVisibility(request.getVisibilityType()));
        activity.setApprovalStatus(teacherDraft ? ActivityApprovalStatus.DRAFT : ActivityApprovalStatus.APPROVED);

        Activity saved = activityRepository.save(activity);
        addHistory(saved, teacherDraft ? "DRAFT_CREATED" : "CREATED", request.getCreatedBy(), null);
        return ActivityResponse.from(saved);
    }

    @Transactional
    public ActivityResponse update(String schoolId, Long id, UpdateActivityRequest request) {
        Activity activity = findActivity(schoolId, id);
        if (request.getTitle() != null && !request.getTitle().isBlank()) activity.setTitle(request.getTitle());
        if (request.getDescription() != null) activity.setDescription(request.getDescription());
        if (request.getActivityDate() != null) activity.setActivityDate(request.getActivityDate());
        if (request.getVisibilityType() != null && !request.getVisibilityType().isBlank()) {
            activity.setVisibilityType(parseVisibility(request.getVisibilityType()));
        }
        Activity saved = activityRepository.save(activity);
        addHistory(saved, "UPDATED", null, null);
        return ActivityResponse.from(saved);
    }

    @Transactional
    public ActivityResponse submit(String schoolId, Long id, ActivityApprovalRequest request) {
        Activity activity = findActivity(schoolId, id);
        activity.setApprovalStatus(ActivityApprovalStatus.SUBMITTED);
        Activity saved = activityRepository.save(activity);
        addHistory(saved, "SUBMITTED", request == null ? null : request.getActionBy(), request == null ? null : request.getRemarks());
        return ActivityResponse.from(saved);
    }

    @Transactional
    public ActivityResponse approve(String schoolId, Long id, ActivityApprovalRequest request) {
        Activity activity = findActivity(schoolId, id);
        activity.setApprovalStatus(ActivityApprovalStatus.APPROVED);
        Activity saved = activityRepository.save(activity);
        addHistory(saved, "APPROVED", request == null ? null : request.getActionBy(), request == null ? null : request.getRemarks());
        return ActivityResponse.from(saved);
    }

    @Transactional
    public ActivityResponse reject(String schoolId, Long id, ActivityApprovalRequest request) {
        Activity activity = findActivity(schoolId, id);
        activity.setApprovalStatus(ActivityApprovalStatus.REJECTED);
        Activity saved = activityRepository.save(activity);
        addHistory(saved, "REJECTED", request == null ? null : request.getActionBy(), request == null ? null : request.getRemarks());
        return ActivityResponse.from(saved);
    }

    @Transactional
    public ActivityResponse publish(String schoolId, Long id, ActivityApprovalRequest request) {
        Activity activity = findActivity(schoolId, id);
        if (activity.getApprovalStatus() == ActivityApprovalStatus.REJECTED) {
            throw new IllegalStateException("Rejected activity cannot be published");
        }
        activity.setApprovalStatus(ActivityApprovalStatus.PUBLISHED);
        activity.setPublishedAt(LocalDateTime.now());
        Activity saved = activityRepository.save(activity);
        addHistory(saved, "PUBLISHED", request == null ? null : request.getActionBy(), request == null ? null : request.getRemarks());
        return ActivityResponse.from(saved);
    }

    public List<ActivityResponse> getActivities(String schoolId) {
        return activityRepository
                .findBySchoolIdIgnoreCaseOrderByActivityDateDescCreatedAtDesc(normalizeSchoolId(schoolId))
                .stream().map(ActivityResponse::from).toList();
    }

    public List<ActivityResponse> pending(String schoolId) {
        return activityRepository
                .findBySchoolIdIgnoreCaseAndApprovalStatusOrderByActivityDateDescCreatedAtDesc(normalizeSchoolId(schoolId), ActivityApprovalStatus.SUBMITTED)
                .stream().map(ActivityResponse::from).toList();
    }

    public List<ActivityResponse> feed(String schoolId) {
        return activityRepository
                .findBySchoolIdIgnoreCaseAndApprovalStatusOrderByActivityDateDescCreatedAtDesc(normalizeSchoolId(schoolId), ActivityApprovalStatus.PUBLISHED)
                .stream().map(ActivityResponse::from).toList();
    }

    public ActivityResponse detail(String schoolId, Long id) {
        return ActivityResponse.from(findActivity(schoolId, id));
    }

    private Activity findActivity(String schoolId, Long id) {
        return activityRepository.findByIdAndSchoolIdIgnoreCase(id, normalizeSchoolId(schoolId))
                .orElseThrow(() -> new IllegalArgumentException("Activity not found for tenant " + normalizeSchoolId(schoolId)));
    }

    private void addHistory(Activity activity, String action, Long actionBy, String remarks) {
        ActivityApprovalHistory history = new ActivityApprovalHistory();
        history.setSchoolId(activity.getSchoolId());
        history.setActivityId(activity.getId());
        history.setAction(action);
        history.setActionBy(actionBy);
        history.setRemarks(remarks);
        approvalHistoryRepository.save(history);
    }

    private ActivityVisibilityType parseVisibility(String visibilityType) {
        if (visibilityType == null || visibilityType.isBlank()) return ActivityVisibilityType.WHOLE_SCHOOL;
        return ActivityVisibilityType.valueOf(visibilityType.trim().toUpperCase(Locale.ROOT));
    }

    private String normalizeSchoolId(String schoolId) {
        return schoolId == null || schoolId.isBlank() ? "TST2" : schoolId.trim().toUpperCase(Locale.ROOT);
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }
}
