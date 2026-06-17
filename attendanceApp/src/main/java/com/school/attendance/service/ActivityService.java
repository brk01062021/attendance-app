package com.school.attendance.service;

import com.school.attendance.dto.ActivityApprovalRequest;
import com.school.attendance.dto.ActivityResponse;
import com.school.attendance.dto.ActivityMediaResponse;
import com.school.attendance.dto.CreateActivityRequest;
import com.school.attendance.dto.UpdateActivityRequest;
import com.school.attendance.entity.Activity;
import com.school.attendance.entity.ActivityApprovalHistory;
import com.school.attendance.entity.ActivityApprovalStatus;
import com.school.attendance.entity.ActivityVisibilityType;
import com.school.attendance.entity.ActivityMedia;
import com.school.attendance.entity.ActivityMediaType;
import com.school.attendance.repository.ActivityApprovalHistoryRepository;
import com.school.attendance.repository.ActivityRepository;
import com.school.attendance.repository.ActivityMediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.school.attendance.storage.FileStorageService;
import com.school.attendance.storage.StoredFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityApprovalHistoryRepository approvalHistoryRepository;
    private final ActivityMediaRepository activityMediaRepository;
    private final FileStorageService fileStorageService;

    public ActivityService(ActivityRepository activityRepository,
                           ActivityApprovalHistoryRepository approvalHistoryRepository,
                           ActivityMediaRepository activityMediaRepository,
                           FileStorageService fileStorageService) {
        this.activityRepository = activityRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.activityMediaRepository = activityMediaRepository;
        this.fileStorageService = fileStorageService;
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
        return toResponse(saved);
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
        return toResponse(saved);
    }

    @Transactional
    public ActivityResponse submit(String schoolId, Long id, ActivityApprovalRequest request) {
        Activity activity = findActivity(schoolId, id);
        activity.setApprovalStatus(ActivityApprovalStatus.SUBMITTED);
        Activity saved = activityRepository.save(activity);
        addHistory(saved, "SUBMITTED", request == null ? null : request.getActionBy(), request == null ? null : request.getRemarks());
        return toResponse(saved);
    }

    @Transactional
    public ActivityResponse approve(String schoolId, Long id, ActivityApprovalRequest request) {
        Activity activity = findActivity(schoolId, id);
        activity.setApprovalStatus(ActivityApprovalStatus.APPROVED);
        Activity saved = activityRepository.save(activity);
        addHistory(saved, "APPROVED", request == null ? null : request.getActionBy(), request == null ? null : request.getRemarks());
        return toResponse(saved);
    }

    @Transactional
    public ActivityResponse reject(String schoolId, Long id, ActivityApprovalRequest request) {
        Activity activity = findActivity(schoolId, id);
        activity.setApprovalStatus(ActivityApprovalStatus.REJECTED);
        Activity saved = activityRepository.save(activity);
        addHistory(saved, "REJECTED", request == null ? null : request.getActionBy(), request == null ? null : request.getRemarks());
        return toResponse(saved);
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
        return toResponse(saved);
    }

    public List<ActivityResponse> getActivities(String schoolId) {
        return activityRepository
                .findBySchoolIdIgnoreCaseOrderByActivityDateDescCreatedAtDesc(normalizeSchoolId(schoolId))
                .stream().map(this::toResponse).toList();
    }

    public List<ActivityResponse> pending(String schoolId) {
        return activityRepository
                .findBySchoolIdIgnoreCaseAndApprovalStatusOrderByActivityDateDescCreatedAtDesc(normalizeSchoolId(schoolId), ActivityApprovalStatus.SUBMITTED)
                .stream().map(this::toResponse).toList();
    }

    public List<ActivityResponse> feed(String schoolId) {
        return activityRepository
                .findBySchoolIdIgnoreCaseAndApprovalStatusOrderByActivityDateDescCreatedAtDesc(normalizeSchoolId(schoolId), ActivityApprovalStatus.PUBLISHED)
                .stream().map(this::toResponse).toList();
    }

    public ActivityResponse detail(String schoolId, Long id) {
        return toResponse(findActivity(schoolId, id));
    }

    @Transactional
    public ActivityMediaResponse uploadMedia(String schoolId, Long activityId, MultipartFile file, String mediaType, Long uploadedBy, Integer displayOrder) {
        Activity activity = findActivity(schoolId, activityId);
        try {
            StoredFile storedFile = fileStorageService.uploadActivityMedia(activity.getSchoolId(), file);
            ActivityMedia media = new ActivityMedia();
            media.setSchoolId(activity.getSchoolId());
            media.setActivityId(activity.getId());
            media.setFileName(storedFile.originalFilename());
            media.setContentType(storedFile.contentType());
            media.setFileSize(storedFile.sizeBytes());
            media.setStorageKey(storedFile.storageKey());
            media.setMediaType(parseMediaType(mediaType, storedFile.contentType()));
            media.setDisplayOrder(displayOrder == null ? nextDisplayOrder(activity.getSchoolId(), activity.getId()) : displayOrder);
            media.setUploadedBy(uploadedBy);
            media.setUploadedAt(LocalDateTime.now());
            ActivityMedia saved = activityMediaRepository.save(media);
            if (activity.getCoverMediaId() == null && saved.getMediaType() == ActivityMediaType.PHOTO) {
                activity.setCoverMediaId(saved.getId());
                activityRepository.save(activity);
            }
            return ActivityMediaResponse.from(saved);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to upload activity media.", ex);
        }
    }

    public List<ActivityMediaResponse> media(String schoolId, Long activityId) {
        Activity activity = findActivity(schoolId, activityId);
        return mediaResponses(activity.getSchoolId(), activity.getId());
    }

    @Transactional
    public void deleteMedia(String schoolId, Long activityId, Long mediaId) {
        Activity activity = findActivity(schoolId, activityId);
        ActivityMedia media = activityMediaRepository.findByIdAndActivityIdAndSchoolIdIgnoreCase(mediaId, activity.getId(), activity.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("Activity media not found."));

        activityMediaRepository.delete(media);

        if (activity.getCoverMediaId() != null && activity.getCoverMediaId().equals(media.getId())) {
            Long nextCoverId = activityMediaRepository
                    .findByActivityIdAndSchoolIdIgnoreCaseOrderByDisplayOrderAscUploadedAtAsc(activity.getId(), activity.getSchoolId())
                    .stream()
                    .filter(item -> item.getMediaType() == ActivityMediaType.PHOTO)
                    .map(ActivityMedia::getId)
                    .findFirst()
                    .orElse(null);
            activity.setCoverMediaId(nextCoverId);
            activityRepository.save(activity);
        }

        addHistory(activity, "MEDIA_REMOVED", null, media.getFileName());
    }

    public ActivityMedia mediaFile(String schoolId, Long activityId, Long mediaId) {
        Activity activity = findActivity(schoolId, activityId);
        return activityMediaRepository.findByIdAndActivityIdAndSchoolIdIgnoreCase(mediaId, activity.getId(), activity.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("Activity media not found."));
    }

    public byte[] mediaBytes(ActivityMedia media) {
        return fileStorageService.read(media.getStorageKey());
    }

    private ActivityResponse toResponse(Activity activity) {
        return ActivityResponse.from(activity, mediaResponses(activity.getSchoolId(), activity.getId()));
    }

    private List<ActivityMediaResponse> mediaResponses(String schoolId, Long activityId) {
        return activityMediaRepository
                .findByActivityIdAndSchoolIdIgnoreCaseOrderByDisplayOrderAscUploadedAtAsc(activityId, normalizeSchoolId(schoolId))
                .stream()
                .map(ActivityMediaResponse::from)
                .toList();
    }

    private int nextDisplayOrder(String schoolId, Long activityId) {
        return activityMediaRepository
                .findByActivityIdAndSchoolIdIgnoreCaseOrderByDisplayOrderAscUploadedAtAsc(activityId, normalizeSchoolId(schoolId))
                .stream()
                .map(ActivityMedia::getDisplayOrder)
                .filter(value -> value != null)
                .max(Integer::compareTo)
                .orElse(-1) + 1;
    }

    private ActivityMediaType parseMediaType(String mediaType, String contentType) {
        if (mediaType != null && !mediaType.isBlank()) {
            return ActivityMediaType.valueOf(mediaType.trim().toUpperCase(Locale.ROOT));
        }
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        return type.startsWith("video/") ? ActivityMediaType.VIDEO : ActivityMediaType.PHOTO;
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
