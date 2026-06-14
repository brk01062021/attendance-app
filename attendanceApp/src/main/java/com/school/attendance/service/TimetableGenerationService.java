package com.school.attendance.service;

import com.school.attendance.dto.AcademicRuleDTO;
import com.school.attendance.dto.AcademicRulesSummaryDTO;
import com.school.attendance.dto.ClassTeacherPoolDTO;
import com.school.attendance.dto.ExistingTimetableImportIssueDTO;
import com.school.attendance.dto.ExistingTimetableImportResponseDTO;
import com.school.attendance.dto.ExistingTimetableImportRowDTO;
import com.school.attendance.dto.ExistingTimetableImportStatusDTO;
import com.school.attendance.dto.ExistingTimetableImportSummaryDTO;
import com.school.attendance.dto.TeacherWorkloadSummaryDTO;
import com.school.attendance.dto.TimetableClassSectionReviewDTO;
import com.school.attendance.dto.TimetableConflictDTO;
import com.school.attendance.dto.TimetableEntryDTO;
import com.school.attendance.dto.TimetableGenerationRequestDTO;
import com.school.attendance.dto.TimetableGenerationResponseDTO;
import com.school.attendance.dto.TimetableRepairResultDTO;
import com.school.attendance.dto.TimetableRolloutReadinessDTO;
import com.school.attendance.dto.TimetablePublishResponseDTO;
import com.school.attendance.dto.TimetableManualEditRequestDTO;
import com.school.attendance.dto.TimetableExportResponseDTO;
import com.school.attendance.dto.PrincipalTimetableIntelligenceDTO;
import com.school.attendance.dto.TimetablePublishAuditDTO;
import com.school.attendance.dto.TimetableBatchSummaryDTO;
import com.school.attendance.dto.TimetableArchiveSummaryDTO;
import com.school.attendance.dto.TimetableBinaryExportDTO;
import com.school.attendance.dto.TimetableLiveResponseDTO;
import com.school.attendance.dto.TimetableNotificationDTO;
import com.school.attendance.dto.TimetableVersionDTO;
import com.school.attendance.entity.TimetableImportFileMetadata;
import com.school.attendance.repository.TimetableImportFileMetadataRepository;
import com.school.attendance.storage.FileStorageService;
import com.school.attendance.storage.StoredFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TimetableGenerationService {

    private static final List<String> DAYS = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY");
    private static final List<String> SUBJECT_ROTATION = List.of("Telugu", "English", "Mathematics", "Science", "Social", "Computer", "Sports", "Library");
    private static final int PERIODS_PER_DAY = 6;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Map<String, TimetableGenerationResponseDTO> generatedBatches = new ConcurrentHashMap<>();
    private final Map<String, List<TimetablePublishAuditDTO>> publishAudits = new ConcurrentHashMap<>();
    private final Map<String, Boolean> publishLocks = new ConcurrentHashMap<>();
    private final Map<String, List<TimetableVersionDTO>> versionHistory = new ConcurrentHashMap<>();
    private final Map<String, List<TimetableNotificationDTO>> timetableNotifications = new ConcurrentHashMap<>();
    private final Map<String, TimetableArchiveSummaryDTO> archiveHistory = new ConcurrentHashMap<>();
    private final Map<String, ExistingTimetableImportResponseDTO> existingTimetableImports = new ConcurrentHashMap<>();
    private final Map<String, String> batchSchoolIds = new ConcurrentHashMap<>();
    private final Map<String, String> activePublishedBatchBySchool = new ConcurrentHashMap<>();
    private volatile String latestBatchId;
    private volatile String latestPublishedBatchId;

    private final FileStorageService fileStorageService;
    private final TimetableImportFileMetadataRepository timetableImportFileMetadataRepository;

    public TimetableGenerationService(FileStorageService fileStorageService, TimetableImportFileMetadataRepository timetableImportFileMetadataRepository) {
        this.fileStorageService = fileStorageService;
        this.timetableImportFileMetadataRepository = timetableImportFileMetadataRepository;
    }

    public TimetableGenerationResponseDTO generate(TimetableGenerationRequestDTO request) {
        TimetableGenerationRequestDTO safeRequest = normalize(request);
        AcademicRulesSummaryDTO academicRulesSummary = validateAcademicRules(safeRequest.getAcademicRules(), safeRequest.getClassNames(), safeRequest.getSections());
        List<TimetableEntryDTO> entries = buildConflictFreeEntries(safeRequest);
        List<TimetableConflictDTO> conflicts = detectConflicts(entries, safeRequest);
        List<TeacherWorkloadSummaryDTO> workload = buildWorkloadSummary(entries);

        TimetableGenerationResponseDTO response = new TimetableGenerationResponseDTO();
        response.setGeneratedBatchId("TT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        response.setTotalClassesScheduled(safeRequest.getClassNames().size() * safeRequest.getSections().size());
        response.setTotalEntries(entries.size());
        response.setEntries(entries);
        response.setConflicts(conflicts);
        response.setWorkloadSummary(workload);
        response.setClassSectionReviews(buildClassSectionReviews(entries));
        response.setAcademicRulesSummary(academicRulesSummary);
        response.setConflictsDetected(conflicts.size());
        response.setOverloadRiskTeachers((int) workload.stream().filter(item -> !"Balanced".equalsIgnoreCase(item.getStatus())).count());
        response.setCompletionPercentage(conflicts.isEmpty() ? 100 : Math.max(70, 100 - Math.min(25, conflicts.size() * 5)));

        generatedBatches.put(response.getGeneratedBatchId(), response);
        addVersion(response.getGeneratedBatchId(), "SYSTEM", "GENERATED", response.getEntries().size(), "Initial generated timetable batch.");
        latestBatchId = response.getGeneratedBatchId();
        return response;
    }

    public TimetableGenerationResponseDTO validate(TimetableGenerationRequestDTO request) {
        TimetableGenerationResponseDTO response = generate(request);
        generatedBatches.remove(response.getGeneratedBatchId());
        return response;
    }

    public TimetableGenerationResponseDTO review(String batchId) {
        return findBatchOrCreateFallback(batchId);
    }

    public List<TimetableConflictDTO> conflicts(String batchId) {
        return findBatchOrCreateFallback(batchId).getConflicts();
    }

    public List<TeacherWorkloadSummaryDTO> workloadAnalysis(String batchId) {
        return findBatchOrCreateFallback(batchId).getWorkloadSummary();
    }

    public List<ClassTeacherPoolDTO> getDefaultPools() {
        List<ClassTeacherPoolDTO> pools = new ArrayList<>();
        pools.add(new ClassTeacherPoolDTO("POOL-1", "Pool 1 • Class 1 Teachers", "1", List.of(101L, 102L, 103L, 104L), List.of("Lakshmi", "Suresh", "Anitha", "Ravi")));
        pools.add(new ClassTeacherPoolDTO("POOL-2", "Pool 2 • Class 2 Teachers", "2", List.of(201L, 202L, 203L, 204L), List.of("Prasad", "Meena", "Kiran", "Jyothi")));
        pools.add(new ClassTeacherPoolDTO("POOL-3", "Pool 3 • Class 3 Teachers", "3", List.of(301L, 302L, 303L, 304L), List.of("Vamsi", "Divya", "Arun", "Rupa")));
        pools.add(new ClassTeacherPoolDTO("POOL-4", "Pool 4 • Class 4 Teachers", "4", List.of(401L, 402L, 403L, 404L), List.of("Kavitha", "Naveen", "Bhanu", "Ramesh")));
        pools.add(new ClassTeacherPoolDTO("POOL-5", "Pool 5 • Class 5 Teachers", "5", List.of(501L, 502L, 503L, 504L), List.of("Geetha", "Mahesh", "Sunitha", "Harsha")));
        pools.add(new ClassTeacherPoolDTO("POOL-6", "Pool 6 • Class 6 Teachers", "6", List.of(601L, 602L, 603L, 604L, 605L), List.of("Ravi Kumar", "Anitha Reddy", "Sailaja", "Mohan", "Fatima")));
        pools.add(new ClassTeacherPoolDTO("POOL-7", "Pool 7 • Class 7 Teachers", "7", List.of(701L, 702L, 703L, 704L, 705L), List.of("Rohit", "Sana", "Deepak", "Pooja", "Gopi")));
        pools.add(new ClassTeacherPoolDTO("POOL-8", "Pool 8 • Class 8 Teachers", "8", List.of(801L, 802L, 803L, 804L, 805L), List.of("Swathi", "Naresh", "Priya", "Varun", "Madhavi")));
        pools.add(new ClassTeacherPoolDTO("POOL-9", "Pool 9 • Class 9 Teachers", "9", List.of(901L, 902L, 903L, 904L, 905L), List.of("Rajesh", "Sirisha", "Vikram", "Keerthi", "Imran")));
        pools.add(new ClassTeacherPoolDTO("POOL-10", "Pool 10 • Class 10 Teachers", "10", List.of(1001L, 1002L, 1003L, 1004L, 1005L), List.of("Ravi Kumar", "Anitha Reddy", "John Paul", "Madhavi", "Srinivas")));
        return pools;
    }

    private TimetableGenerationRequestDTO normalize(TimetableGenerationRequestDTO request) {
        TimetableGenerationRequestDTO normalized = request == null ? new TimetableGenerationRequestDTO() : request;
        if (isBlank(normalized.getAcademicYear())) normalized.setAcademicYear("2026-2027");
        if (isBlank(normalized.getGenerationMode())) normalized.setGenerationMode("ANNUAL");
        normalized.setClassNames(uniqueOrDefault(normalized.getClassNames(), List.of("10")));
        normalized.setSections(uniqueOrDefault(normalized.getSections(), List.of("A")));

        if (normalized.getSelectedTeacherPools() == null || normalized.getSelectedTeacherPools().isEmpty()) {
            normalized.setSelectedTeacherPools(getDefaultPools().stream()
                    .filter(pool -> normalized.getClassNames().contains(pool.getClassName()))
                    .toList());
        }

        Set<Long> teachers = new LinkedHashSet<>();
        if (normalized.getTeacherIds() != null) teachers.addAll(normalized.getTeacherIds());
        if (normalized.getSelectedTeacherPools() != null) {
            normalized.getSelectedTeacherPools().forEach(pool -> {
                if (pool.getTeacherIds() != null) teachers.addAll(pool.getTeacherIds());
            });
        }
        if (teachers.isEmpty()) teachers.addAll(List.of(1001L, 1002L, 1003L, 1004L));
        normalized.setTeacherIds(new ArrayList<>(teachers));
        if (normalized.getAcademicRules() == null || normalized.getAcademicRules().isEmpty()) {
            normalized.setAcademicRules(defaultAcademicRulesForClasses(normalized.getClassNames()));
        }
        return normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private List<String> uniqueOrDefault(List<String> values, List<String> fallback) {
        List<String> cleaned = values == null ? List.of() : values.stream()
                                                            .filter(value -> value != null && !value.isBlank())
                                                            .map(String::trim)
                                                            .distinct()
                                                            .toList();
        return cleaned.isEmpty() ? new ArrayList<>(fallback) : new ArrayList<>(cleaned);
    }

    private List<TimetableEntryDTO> buildConflictFreeEntries(TimetableGenerationRequestDTO request) {
        List<TimetableEntryDTO> entries = new ArrayList<>();
        Map<String, Set<Long>> busyTeachersBySlot = new HashMap<>();
        Map<Long, Integer> weeklyLoadByTeacher = new HashMap<>();
        Map<Long, Map<String, Set<Integer>>> teacherDayPeriods = new HashMap<>();
        Map<String, Long> continuityTeacherByClassSubject = new HashMap<>();
        int sequence = 1;

        for (String day : DAYS) {
            for (int period = 1; period <= PERIODS_PER_DAY; period++) {
                for (String className : request.getClassNames()) {
                    List<Long> classTeacherIds = teacherIdsForClass(request, className);
                    for (String section : request.getSections()) {
                        String subject = subjectFor(request, className, section, day, period);
                        Long teacherId = chooseAvailableTeacher(
                                request,
                                className,
                                section,
                                subject,
                                day,
                                period,
                                classTeacherIds,
                                busyTeachersBySlot,
                                weeklyLoadByTeacher,
                                teacherDayPeriods,
                                continuityTeacherByClassSubject
                        );
                        String slotKey = slotKey(day, period);
                        busyTeachersBySlot.computeIfAbsent(slotKey, key -> new HashSet<>()).add(teacherId);
                        weeklyLoadByTeacher.merge(teacherId, 1, Integer::sum);
                        teacherDayPeriods.computeIfAbsent(teacherId, key -> new HashMap<>())
                                .computeIfAbsent(day, key -> new HashSet<>())
                                .add(period);
                        continuityTeacherByClassSubject.putIfAbsent(className + "|" + section + "|" + subject, teacherId);

                        boolean lab = "Computer".equals(subject) || ("Science".equals(subject) && period == 5);
                        boolean sports = "Sports".equals(subject);
                        LocalTime start = LocalTime.of(9, 0).plusMinutes((long) (period - 1) * 45);
                        LocalTime end = start.plusMinutes(40);
                        entries.add(new TimetableEntryDTO(
                                "TT-" + sequence++,
                                className,
                                section,
                                subject,
                                teacherId,
                                teacherNameFor(request, teacherId),
                                day,
                                period,
                                "R-" + className + section,
                                start.format(TIME_FORMATTER),
                                end.format(TIME_FORMATTER),
                                lab,
                                sports,
                                false
                        ));
                    }
                }
            }
        }
        return entries;
    }

    private Long chooseAvailableTeacher(TimetableGenerationRequestDTO request,
                                        String className,
                                        String section,
                                        String subject,
                                        String day,
                                        int period,
                                        List<Long> classTeacherIds,
                                        Map<String, Set<Long>> busyTeachersBySlot,
                                        Map<Long, Integer> weeklyLoadByTeacher,
                                        Map<Long, Map<String, Set<Integer>>> teacherDayPeriods,
                                        Map<String, Long> continuityTeacherByClassSubject) {
        String slotKey = slotKey(day, period);
        Set<Long> busyTeachers = busyTeachersBySlot.getOrDefault(slotKey, Set.of());
        String continuityKey = className + "|" + section + "|" + subject;
        Long continuityTeacher = continuityTeacherByClassSubject.get(continuityKey);
        if (Boolean.TRUE.equals(request.getSameTeacherContinuityEnabled()) && continuityTeacher != null && !busyTeachers.contains(continuityTeacher)) {
            return continuityTeacher;
        }

        List<Long> candidates = classTeacherIds.stream()
                .filter(teacherId -> !busyTeachers.contains(teacherId))
                .toList();
        if (candidates.isEmpty()) {
            candidates = request.getTeacherIds().stream()
                    .filter(teacherId -> !busyTeachers.contains(teacherId))
                    .toList();
        }
        if (candidates.isEmpty()) {
            return classTeacherIds.isEmpty() ? request.getTeacherIds().get(0) : classTeacherIds.get(0);
        }

        return candidates.stream()
                .min(Comparator
                        .comparingInt((Long teacherId) -> weeklyLoadByTeacher.getOrDefault(teacherId, 0))
                        .thenComparingInt(teacherId -> gapPenalty(teacherId, day, period, teacherDayPeriods))
                        .thenComparingLong(Long::longValue))
                .orElse(candidates.get(0));
    }

    private int gapPenalty(Long teacherId, String day, int period, Map<Long, Map<String, Set<Integer>>> teacherDayPeriods) {
        Set<Integer> periods = teacherDayPeriods.getOrDefault(teacherId, Map.of()).getOrDefault(day, Set.of());
        if (periods.isEmpty()) return 0;
        if (periods.contains(period - 1) || periods.contains(period + 1)) return -1;
        int nearest = periods.stream().mapToInt(existing -> Math.abs(existing - period)).min().orElse(0);
        return nearest > 1 ? nearest : 0;
    }

    private String subjectFor(TimetableGenerationRequestDTO request, String className, String section, String day, int period) {
        if (Boolean.TRUE.equals(request.getAcademicRulesEngineEnabled()) && request.getAcademicRules() != null && !request.getAcademicRules().isEmpty()) {
            List<AcademicRuleDTO> classRules = rulesForClass(request.getAcademicRules(), className);
            for (AcademicRuleDTO rule : classRules) {
                if (Boolean.TRUE.equals(rule.getFixedPeriodRequired()) && rule.getPreferredPeriodNumber() != null && rule.getPreferredPeriodNumber() == period) {
                    return rule.getSubjectName();
                }
            }
            List<String> weeklyPlan = buildWeeklySubjectPlan(classRules);
            if (!weeklyPlan.isEmpty()) {
                int dayIndex = Math.max(0, DAYS.indexOf(day));
                int slotIndex = Math.floorMod(dayIndex * PERIODS_PER_DAY + period - 1 + Math.abs(section.hashCode() % 3), weeklyPlan.size());
                return weeklyPlan.get(slotIndex);
            }
        }
        int dayIndex = DAYS.indexOf(day);
        int seed = Math.floorMod(className.hashCode() + section.hashCode() + dayIndex + period, SUBJECT_ROTATION.size());
        return SUBJECT_ROTATION.get(seed).replace("-", "");
    }

    private List<AcademicRuleDTO> rulesForClass(List<AcademicRuleDTO> rules, String className) {
        List<AcademicRuleDTO> exact = rules.stream()
                .filter(rule -> className.equals(rule.getClassName()))
                .toList();
        if (!exact.isEmpty()) return exact;
        return rules.stream()
                .filter(rule -> rule.getClassName() == null || rule.getClassName().isBlank() || "DEFAULT".equalsIgnoreCase(rule.getClassName()))
                .toList();
    }

    private List<String> buildWeeklySubjectPlan(List<AcademicRuleDTO> classRules) {
        List<AcademicRuleDTO> sortedRules = classRules.stream()
                .sorted(Comparator.comparing((AcademicRuleDTO rule) -> priorityWeight(rule.getPriority()))
                        .thenComparing(AcademicRuleDTO::getSubjectName, Comparator.nullsLast(String::compareTo)))
                .toList();
        List<String> plan = new ArrayList<>();
        for (AcademicRuleDTO rule : sortedRules) {
            int count = Math.max(0, rule.getWeeklyPeriods() == null ? 0 : rule.getWeeklyPeriods());
            for (int index = 0; index < count; index++) {
                if (!isBlank(rule.getSubjectName())) plan.add(rule.getSubjectName());
            }
        }
        int cursor = 0;
        while (plan.size() < DAYS.size() * PERIODS_PER_DAY && !sortedRules.isEmpty()) {
            AcademicRuleDTO next = sortedRules.get(cursor % sortedRules.size());
            if (!isBlank(next.getSubjectName())) plan.add(next.getSubjectName());
            cursor++;
        }
        return plan;
    }

    private int priorityWeight(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) return 0;
        if ("MEDIUM".equalsIgnoreCase(priority)) return 1;
        return 2;
    }

    private String slotKey(String day, int period) {
        return day + "|" + period;
    }

    private List<Long> teacherIdsForClass(TimetableGenerationRequestDTO request, String className) {
        if (request.getSelectedTeacherPools() != null) {
            for (ClassTeacherPoolDTO pool : request.getSelectedTeacherPools()) {
                if (className.equals(pool.getClassName()) && pool.getTeacherIds() != null && !pool.getTeacherIds().isEmpty()) {
                    return pool.getTeacherIds();
                }
            }
        }
        return request.getTeacherIds().isEmpty() ? List.of(1001L) : request.getTeacherIds();
    }

    private String teacherNameFor(TimetableGenerationRequestDTO request, Long teacherId) {
        if (teacherId == null) return "Unassigned";
        if (request.getSelectedTeacherPools() != null) {
            for (ClassTeacherPoolDTO pool : request.getSelectedTeacherPools()) {
                List<Long> ids = pool.getTeacherIds();
                List<String> names = pool.getTeacherNames();
                if (ids != null && names != null) {
                    int index = ids.indexOf(teacherId);
                    if (index >= 0 && index < names.size()) return names.get(index);
                }
            }
        }
        return "Teacher " + teacherId;
    }

    private List<TimetableConflictDTO> detectConflicts(List<TimetableEntryDTO> entries, TimetableGenerationRequestDTO request) {
        List<TimetableConflictDTO> conflicts = new ArrayList<>();
        Map<String, List<TimetableEntryDTO>> teacherPeriodMap = entries.stream()
                .filter(entry -> entry.getTeacherId() != null)
                .collect(Collectors.groupingBy(entry -> entry.getTeacherId() + "|" + entry.getDayOfWeek() + "|" + entry.getPeriodNumber(), LinkedHashMap::new, Collectors.toList()));

        int conflictNumber = 1;
        for (List<TimetableEntryDTO> overlap : teacherPeriodMap.values()) {
            if (overlap.size() > 1) {
                TimetableEntryDTO first = overlap.get(0);
                overlap.forEach(entry -> entry.setConflict(true));
                conflicts.add(new TimetableConflictDTO(
                        "C-" + conflictNumber++,
                        "HIGH",
                        "TEACHER_OVERLAP",
                        "Teacher double-booked",
                        first.getTeacherName() + " is assigned to " + overlap.size() + " classes in the same period.",
                        first.getClassName(),
                        first.getSection(),
                        first.getTeacherName(),
                        first.getDayOfWeek(),
                        first.getPeriodNumber()
                ));
            }
        }

        Map<String, List<TimetableEntryDTO>> classPeriodMap = entries.stream()
                .filter(entry -> !isBlank(entry.getClassName()))
                .filter(entry -> !isBlank(entry.getSection()))
                .filter(entry -> !isBlank(entry.getDayOfWeek()))
                .filter(entry -> entry.getPeriodNumber() != null)
                .collect(Collectors.groupingBy(entry -> safeText(entry.getClassName()) + "|" + safeText(entry.getSection()) + "|" + normalizeDay(entry.getDayOfWeek()) + "|" + entry.getPeriodNumber(), LinkedHashMap::new, Collectors.toList()));

        for (List<TimetableEntryDTO> overlap : classPeriodMap.values()) {
            if (overlap.size() > 1) {
                TimetableEntryDTO first = overlap.get(0);
                overlap.forEach(entry -> entry.setConflict(true));
                conflicts.add(new TimetableConflictDTO(
                        "C-" + conflictNumber++,
                        "HIGH",
                        "CLASS_SLOT_OVERLAP",
                        "Duplicate class-period slot",
                        first.getClassName() + "-" + first.getSection() + " has " + overlap.size() + " subjects in the same period.",
                        first.getClassName(),
                        first.getSection(),
                        first.getTeacherName(),
                        first.getDayOfWeek(),
                        first.getPeriodNumber()
                ));
            }
        }

        if (Boolean.TRUE.equals(request.getPreventConsecutiveLabsEnabled())) {
            Map<String, List<TimetableEntryDTO>> classDayMap = entries.stream()
                    .collect(Collectors.groupingBy(entry -> entry.getClassName() + "|" + entry.getSection() + "|" + entry.getDayOfWeek(), LinkedHashMap::new, Collectors.toList()));
            for (List<TimetableEntryDTO> dayEntries : classDayMap.values()) {
                List<TimetableEntryDTO> sorted = dayEntries.stream().sorted(Comparator.comparing(TimetableEntryDTO::getPeriodNumber)).toList();
                for (int index = 1; index < sorted.size(); index++) {
                    TimetableEntryDTO previous = sorted.get(index - 1);
                    TimetableEntryDTO current = sorted.get(index);
                    if (Boolean.TRUE.equals(previous.getIsLab()) && Boolean.TRUE.equals(current.getIsLab())) {
                        current.setConflict(true);
                        conflicts.add(new TimetableConflictDTO(
                                "C-" + conflictNumber++,
                                "MEDIUM",
                                "SUBJECT_OVERLOAD",
                                "Consecutive lab risk",
                                "Two lab-heavy periods are placed continuously. Review before publishing.",
                                current.getClassName(),
                                current.getSection(),
                                current.getTeacherName(),
                                current.getDayOfWeek(),
                                current.getPeriodNumber()
                        ));
                    }
                }
            }
        }
        return conflicts;
    }

    private List<TimetableClassSectionReviewDTO> buildClassSectionReviews(List<TimetableEntryDTO> entries) {
        Map<String, List<TimetableEntryDTO>> grouped = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getClassName() + "|" + entry.getSection(), LinkedHashMap::new, Collectors.toList()));
        List<TimetableClassSectionReviewDTO> reviews = new ArrayList<>();
        for (Map.Entry<String, List<TimetableEntryDTO>> item : grouped.entrySet()) {
            List<TimetableEntryDTO> sortedEntries = item.getValue().stream()
                    .sorted(Comparator.comparing((TimetableEntryDTO entry) -> DAYS.indexOf(entry.getDayOfWeek()))
                            .thenComparing(TimetableEntryDTO::getPeriodNumber))
                    .toList();
            TimetableEntryDTO first = sortedEntries.get(0);
            int conflictCount = (int) sortedEntries.stream().filter(entry -> Boolean.TRUE.equals(entry.getConflict())).count();
            reviews.add(new TimetableClassSectionReviewDTO(
                    first.getClassName(),
                    first.getSection(),
                    first.getClassName() + "-" + first.getSection(),
                    sortedEntries.size(),
                    conflictCount,
                    sortedEntries
            ));
        }
        return reviews;
    }


    public List<AcademicRuleDTO> getDefaultAcademicRules(List<String> classNames) {
        List<String> classes = uniqueOrDefault(classNames, List.of("1", "2"));
        return defaultAcademicRulesForClasses(classes);
    }

    public AcademicRulesSummaryDTO validateAcademicRules(List<AcademicRuleDTO> rules, List<String> classNames, List<String> sections) {
        List<String> safeClasses = uniqueOrDefault(classNames, List.of("1"));
        List<String> safeSections = uniqueOrDefault(sections, List.of("A"));
        List<AcademicRuleDTO> safeRules = rules == null || rules.isEmpty() ? defaultAcademicRulesForClasses(safeClasses) : rules;
        int availableSlots = safeClasses.size() * safeSections.size() * DAYS.size() * PERIODS_PER_DAY;
        int required = safeRules.stream().mapToInt(rule -> Math.max(0, rule.getWeeklyPeriods() == null ? 0 : rule.getWeeklyPeriods())).sum() * safeSections.size();
        int theory = totalByType(safeRules, "THEORY") * safeSections.size();
        int lab = totalByType(safeRules, "LAB") * safeSections.size();
        int sports = totalByType(safeRules, "SPORTS") * safeSections.size();
        int activity = totalByType(safeRules, "ACTIVITY") * safeSections.size();
        List<String> warnings = new ArrayList<>();
        if (required > availableSlots) warnings.add("Academic rules require more periods than available weekly timetable slots.");
        if (safeRules.stream().noneMatch(rule -> "Mathematics".equalsIgnoreCase(rule.getSubjectName()))) warnings.add("Mathematics rule is missing for one or more selected classes.");
        if (safeRules.stream().noneMatch(rule -> "English".equalsIgnoreCase(rule.getSubjectName()))) warnings.add("English rule is missing for one or more selected classes.");
        if (lab == 0) warnings.add("No lab/activity rule configured. Add Computer or Science lab before final production rollout if applicable.");
        return new AcademicRulesSummaryDTO(safeRules.size(), required, availableSlots, theory, lab, sports, activity, required <= availableSlots, warnings);
    }

    private int totalByType(List<AcademicRuleDTO> rules, String type) {
        return rules.stream()
                .filter(rule -> type.equalsIgnoreCase(rule.getSubjectType()))
                .mapToInt(rule -> Math.max(0, rule.getWeeklyPeriods() == null ? 0 : rule.getWeeklyPeriods()))
                .sum();
    }

    private List<AcademicRuleDTO> defaultAcademicRulesForClasses(List<String> classNames) {
        List<AcademicRuleDTO> rules = new ArrayList<>();
        for (String className : classNames) {
            rules.add(new AcademicRuleDTO("AR-" + className + "-TEL", className, "Telugu", "THEORY", 5, false, null, true, "HIGH"));
            rules.add(new AcademicRuleDTO("AR-" + className + "-ENG", className, "English", "THEORY", 5, false, null, true, "HIGH"));
            rules.add(new AcademicRuleDTO("AR-" + className + "-MAT", className, "Mathematics", "THEORY", 6, false, null, true, "HIGH"));
            rules.add(new AcademicRuleDTO("AR-" + className + "-SCI", className, "Science", "THEORY", 5, false, null, true, "HIGH"));
            rules.add(new AcademicRuleDTO("AR-" + className + "-SOC", className, "Social", "THEORY", 5, false, null, true, "MEDIUM"));
            rules.add(new AcademicRuleDTO("AR-" + className + "-COM", className, "Computer", "LAB", 2, true, 5, true, "MEDIUM"));
            rules.add(new AcademicRuleDTO("AR-" + className + "-SPO", className, "Sports", "SPORTS", 2, true, 6, false, "LOW"));
            rules.add(new AcademicRuleDTO("AR-" + className + "-LIB", className, "Library", "ACTIVITY", 1, false, null, false, "LOW"));
        }
        return rules;
    }


    public TimetableRepairResultDTO repair(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        refreshBatch(batch);
        int before = batch.getConflicts() == null ? 0 : batch.getConflicts().size();
        List<String> actions = new ArrayList<>();

        // Day 25 production rule for Existing Timetable Import:
        // Auto Conflict Repair must not generate a new timetable or search for free slots first.
        // It repairs the uploaded timetable by same-day period reshuffling inside affected
        // class-section timetables, while validating all teachers connected to that day.
        OptimizationOutcome outcome = optimizeExistingImportBySameDayReshuffle(batch.getEntries());
        actions.addAll(outcome.actions);

        refreshBatch(batch);
        int after = batch.getConflicts() == null ? 0 : batch.getConflicts().size();
        int resolved = Math.max(0, before - after);

        if (after == 0) {
            actions.add("Auto Repair completed successfully. " + before + " → 0 conflicts. Timetable Ready To Publish after revalidation.");
        } else if (resolved > 0) {
            actions.add("Auto Repair partially completed. " + resolved + " conflicts resolved. " + after + " conflicts remain. Run Auto Repair again; use Manual Edit only for intentional Admin/Principal customization.");
        } else {
            actions.add("Auto Repair attempted. No valid timetable optimization found. " + after + " conflicts remain.");
        }

        TimetableRepairResultDTO result = new TimetableRepairResultDTO();
        result.setBatchId(batch.getGeneratedBatchId());
        result.setConflictsBefore(before);
        result.setConflictsAfter(after);
        result.setRepairedItems(outcome.movesApplied);
        result.setPublishReady(after == 0);
        result.setActions(actions);
        result.setTimetable(batch);
        generatedBatches.put(batch.getGeneratedBatchId(), batch);
        syncExistingImportAfterRevalidation(batch, after == 0 ? "AUTO_REPAIR_READY" : (resolved > 0 ? "AUTO_REPAIR_PARTIAL" : "AUTO_REPAIR_ATTEMPTED"));
        String versionMessage = after == 0
                ? "Auto Repair completed successfully. " + before + " → 0 conflicts. Timetable Ready To Publish. Teacher, subject, and class ownership preserved."
                : (resolved > 0
                   ? "Auto Repair partially completed. " + resolved + " conflicts resolved. " + after + " conflicts remain. Teacher, subject, and class ownership preserved."
                   : "Auto Repair attempted. No valid timetable optimization found. " + after + " conflicts remain. Teacher, subject, and class ownership preserved.");
        addVersion(batch.getGeneratedBatchId(), "SYSTEM", "AUTO_REPAIR", batch.getEntries().size(), versionMessage);
        return result;
    }

    private OptimizationOutcome optimizeExistingImportBySameDayReshuffle(List<TimetableEntryDTO> entries) {
        OptimizationOutcome outcome = new OptimizationOutcome();
        if (entries == null || entries.isEmpty()) return outcome;

        int previous = countBlockingConflicts(entries);
        int initial = previous;
        List<EntrySlotSnapshot> bestSnapshot = snapshot(entries);
        int guard = Math.max(250, entries.size() * 8);

        while (previous > 0 && guard-- > 0) {
            SameDaySwap bestSwap = null;

            List<List<TimetableEntryDTO>> teacherConflicts = teacherOverlapGroups(entries);
            for (List<TimetableEntryDTO> conflictGroup : teacherConflicts) {
                List<TimetableEntryDTO> orderedConflictEntries = conflictGroup.stream()
                        .sorted(Comparator.comparing((TimetableEntryDTO entry) -> safeText(entry.getClassName()))
                                .thenComparing(entry -> safeText(entry.getSection()))
                                .thenComparing(entry -> safeText(entry.getSubjectName()))
                                .thenComparing(entry -> safeText(entry.getId())))
                        .toList();

                for (TimetableEntryDTO target : orderedConflictEntries) {
                    String day = normalizeDay(target.getDayOfWeek());
                    if (isBlank(day)) continue;
                    List<TimetableEntryDTO> sameDayCandidates = sameClassSameDayCandidates(entries, target);
                    for (TimetableEntryDTO candidate : sameDayCandidates) {
                        bestSwap = betterSameDaySwap(entries, target, candidate, previous, bestSwap);
                    }
                }
            }

            // If teacher conflicts are not directly improved, try class duplicate rows using the same rule:
            // keep the imported class/day timetable, and reshuffle period numbers inside that same day.
            if (bestSwap == null) {
                List<List<TimetableEntryDTO>> classConflicts = classSectionSlotOverlapGroups(entries);
                for (List<TimetableEntryDTO> conflictGroup : classConflicts) {
                    for (TimetableEntryDTO target : conflictGroup) {
                        List<TimetableEntryDTO> sameDayCandidates = sameClassSameDayCandidates(entries, target);
                        for (TimetableEntryDTO candidate : sameDayCandidates) {
                            bestSwap = betterSameDaySwap(entries, target, candidate, previous, bestSwap);
                        }
                    }
                }
            }

            if (bestSwap == null || bestSwap.after >= previous) break;

            swapSlots(bestSwap.left, bestSwap.right);
            outcome.movesApplied++;
            outcome.actions.add("Same-day period reshuffle applied: " + describeEntryForAudit(bestSwap.left)
                    + " swapped with " + describeEntryForAudit(bestSwap.right)
                    + ". Conflicts " + previous + " → " + bestSwap.after + ".");
            previous = bestSwap.after;
            bestSnapshot = snapshot(entries);
        }

        restore(entries, bestSnapshot);
        int finalConflicts = countBlockingConflicts(entries);
        if (finalConflicts >= initial) {
            outcome.movesApplied = 0;
            outcome.actions.clear();
        }
        return outcome;
    }

    private SameDaySwap betterSameDaySwap(List<TimetableEntryDTO> entries, TimetableEntryDTO left, TimetableEntryDTO right, int currentConflicts, SameDaySwap bestSwap) {
        if (left == null || right == null || left == right) return bestSwap;
        if (!equalsText(left.getClassName(), right.getClassName())) return bestSwap;
        if (!equalsText(left.getSection(), right.getSection())) return bestSwap;
        if (!equalsText(normalizeDay(left.getDayOfWeek()), normalizeDay(right.getDayOfWeek()))) return bestSwap;
        if (Objects.equals(left.getPeriodNumber(), right.getPeriodNumber())) return bestSwap;

        swapSlots(left, right);
        int after = countBlockingConflicts(entries);
        swapSlots(left, right);

        if (after >= currentConflicts) return bestSwap;
        if (bestSwap == null || after < bestSwap.after || (after == bestSwap.after && sameDaySwapTieBreak(left, right) < sameDaySwapTieBreak(bestSwap.left, bestSwap.right))) {
            return new SameDaySwap(left, right, after);
        }
        return bestSwap;
    }

    private int sameDaySwapTieBreak(TimetableEntryDTO left, TimetableEntryDTO right) {
        int leftPeriod = left.getPeriodNumber() == null ? 99 : left.getPeriodNumber();
        int rightPeriod = right.getPeriodNumber() == null ? 99 : right.getPeriodNumber();
        return Math.abs(leftPeriod - rightPeriod) * 100 + Math.min(leftPeriod, rightPeriod);
    }

    private List<TimetableEntryDTO> sameClassSameDayCandidates(List<TimetableEntryDTO> entries, TimetableEntryDTO target) {
        if (target == null || isBlank(target.getClassName()) || isBlank(target.getSection()) || isBlank(target.getDayOfWeek())) return List.of();
        String day = normalizeDay(target.getDayOfWeek());
        return entries.stream()
                .filter(entry -> entry != target)
                .filter(entry -> equalsText(entry.getClassName(), target.getClassName()))
                .filter(entry -> equalsText(entry.getSection(), target.getSection()))
                .filter(entry -> equalsText(normalizeDay(entry.getDayOfWeek()), day))
                .filter(entry -> entry.getPeriodNumber() != null)
                .sorted(Comparator.comparingInt((TimetableEntryDTO entry) -> Math.abs((entry.getPeriodNumber() == null ? 0 : entry.getPeriodNumber()) - (target.getPeriodNumber() == null ? 0 : target.getPeriodNumber())))
                        .thenComparing(entry -> safeText(entry.getSubjectName()))
                        .thenComparing(entry -> safeText(entry.getTeacherName()))
                        .thenComparing(entry -> safeText(entry.getId())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String describeEntryForAudit(TimetableEntryDTO entry) {
        if (entry == null) return "timetable entry";
        return safeText(entry.getClassName()) + "-" + safeText(entry.getSection())
                + " " + normalizeDay(entry.getDayOfWeek()) + " P" + entry.getPeriodNumber()
                + " " + safeText(entry.getSubjectName())
                + " (" + safeText(entry.getTeacherName()) + ")";
    }

    private SlotRef chooseBestSlot(TimetableEntryDTO entry, List<SlotRef> availableSlots, Map<String, TimetableEntryDTO> occupiedTeacherSlots, Map<String, Integer> dailySubjectCounts, int pass) {
        if (availableSlots == null || availableSlots.isEmpty()) return null;
        Long teacherId = entry.getTeacherId();
        return availableSlots.stream()
                .min(Comparator.comparingInt((SlotRef slot) -> slotPenalty(entry, slot, teacherId, occupiedTeacherSlots, dailySubjectCounts, pass))
                        .thenComparing(slot -> slot.day)
                        .thenComparingInt(slot -> slot.period))
                .orElse(availableSlots.get(0));
    }

    private int slotPenalty(TimetableEntryDTO entry, SlotRef slot, Long teacherId, Map<String, TimetableEntryDTO> occupiedTeacherSlots, Map<String, Integer> dailySubjectCounts, int pass) {
        int penalty = 0;
        if (teacherId != null && occupiedTeacherSlots.containsKey(teacherSlotKey(teacherId, slot.day, slot.period))) penalty += 10_000;
        String subjectKey = slot.day + "|" + safeText(entry.getSubjectName()).toUpperCase(Locale.ROOT);
        int sameSubjectToday = dailySubjectCounts.getOrDefault(subjectKey, 0);
        if (sameSubjectToday > 0) penalty += 100 + sameSubjectToday * 25;
        int originalDistance = Math.abs(slot.period - (entry.getPeriodNumber() == null ? slot.period : entry.getPeriodNumber()));
        penalty += pass % 3 == 0 ? originalDistance : Math.max(0, 7 - slot.period);
        return penalty;
    }

    private int classConflictWeight(List<TimetableEntryDTO> entries) {
        if (entries == null) return 0;
        return (int) entries.stream().filter(entry -> Boolean.TRUE.equals(entry.getConflict())).count();
    }

    private int optimizationScore(List<TimetableEntryDTO> entries) {
        return detectConflictsForOptimization(entries).size() * 10_000 + teacherLoadPenalty(entries) + dailySubjectPenalty(entries);
    }

    private List<TimetableConflictDTO> detectConflictsForOptimization(List<TimetableEntryDTO> entries) {
        TimetableGenerationRequestDTO request = new TimetableGenerationRequestDTO();
        request.setPreventConsecutiveLabsEnabled(true);
        List<EntryConflictSnapshot> flags = entries.stream().map(entry -> new EntryConflictSnapshot(entry, entry.getConflict())).toList();
        List<TimetableConflictDTO> conflicts = detectConflicts(entries, request);
        for (EntryConflictSnapshot flag : flags) flag.entry.setConflict(flag.conflict);
        return conflicts;
    }

    private int teacherLoadPenalty(List<TimetableEntryDTO> entries) {
        Map<String, Long> dailyLoad = entries.stream()
                .filter(entry -> entry.getTeacherId() != null)
                .filter(entry -> !isBlank(entry.getDayOfWeek()))
                .collect(Collectors.groupingBy(entry -> entry.getTeacherId() + "|" + normalizeDay(entry.getDayOfWeek()), Collectors.counting()));
        return dailyLoad.values().stream().mapToInt(count -> count > 7 ? (int) ((count - 7) * 50) : 0).sum();
    }

    private int dailySubjectPenalty(List<TimetableEntryDTO> entries) {
        Map<String, Long> grouped = entries.stream()
                .filter(entry -> !isBlank(entry.getClassName()))
                .filter(entry -> !isBlank(entry.getSection()))
                .filter(entry -> !isBlank(entry.getSubjectName()))
                .filter(entry -> !isBlank(entry.getDayOfWeek()))
                .collect(Collectors.groupingBy(entry -> safeText(entry.getClassName()) + "|" + safeText(entry.getSection()) + "|" + normalizeDay(entry.getDayOfWeek()) + "|" + safeText(entry.getSubjectName()).toUpperCase(Locale.ROOT), Collectors.counting()));
        return grouped.values().stream().mapToInt(count -> count > 1 ? (int) ((count - 1) * 30) : 0).sum();
    }

    private List<SlotRef> buildSchoolSlots(List<String> days, int maxPeriod) {
        List<SlotRef> slots = new ArrayList<>();
        for (String day : days) {
            for (int period = 1; period <= maxPeriod; period++) {
                slots.add(new SlotRef(day, period));
            }
        }
        return slots;
    }

    private void applySlot(TimetableEntryDTO entry, SlotRef slot) {
        entry.setDayOfWeek(slot.day);
        entry.setPeriodNumber(slot.period);
        applyPeriodTimes(entry, slot.period);
    }

    private String teacherSlotKey(Long teacherId, String day, Integer period) {
        return teacherId + "|" + normalizeDay(day) + "|" + period;
    }

    private List<EntrySlotSnapshot> snapshot(List<TimetableEntryDTO> entries) {
        return entries.stream().map(EntrySlotSnapshot::new).collect(Collectors.toCollection(ArrayList::new));
    }

    private void restore(List<TimetableEntryDTO> entries, List<EntrySlotSnapshot> snapshots) {
        if (entries == null || snapshots == null) return;
        Map<String, EntrySlotSnapshot> byId = snapshots.stream().collect(Collectors.toMap(item -> item.id, item -> item, (a, b) -> a, LinkedHashMap::new));
        for (TimetableEntryDTO entry : entries) {
            EntrySlotSnapshot snapshot = byId.get(String.valueOf(entry.getId()));
            if (snapshot == null) continue;
            entry.setDayOfWeek(snapshot.day);
            entry.setPeriodNumber(snapshot.period);
            entry.setStartTime(snapshot.startTime);
            entry.setEndTime(snapshot.endTime);
            entry.setRoomNumber(snapshot.roomNumber);
        }
    }

    private List<EntrySlotSnapshot> snapshotFromInitialIfNeeded(List<EntrySlotSnapshot> snapshots, List<TimetableEntryDTO> entries) {
        return snapshots == null || snapshots.isEmpty() ? snapshot(entries) : snapshots;
    }

    private static class OptimizationOutcome {
        private int movesApplied;
        private final List<String> actions = new ArrayList<>();
    }

    private static class SlotRef {
        private final String day;
        private final Integer period;
        private SlotRef(String day, Integer period) {
            this.day = day;
            this.period = period;
        }
        @Override public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof SlotRef slotRef)) return false;
            return Objects.equals(day, slotRef.day) && Objects.equals(period, slotRef.period);
        }
        @Override public int hashCode() { return Objects.hash(day, period); }
    }

    private static class SameDaySwap {
        private final TimetableEntryDTO left;
        private final TimetableEntryDTO right;
        private final int after;
        private SameDaySwap(TimetableEntryDTO left, TimetableEntryDTO right, int after) {
            this.left = left;
            this.right = right;
            this.after = after;
        }
    }

    private static class EntrySlotSnapshot {
        private final String id;
        private final String day;
        private final Integer period;
        private final String startTime;
        private final String endTime;
        private final String roomNumber;
        private EntrySlotSnapshot(TimetableEntryDTO entry) {
            this.id = String.valueOf(entry.getId());
            this.day = entry.getDayOfWeek();
            this.period = entry.getPeriodNumber();
            this.startTime = entry.getStartTime();
            this.endTime = entry.getEndTime();
            this.roomNumber = entry.getRoomNumber();
        }
    }

    private static class EntryConflictSnapshot {
        private final TimetableEntryDTO entry;
        private final Boolean conflict;
        private EntryConflictSnapshot(TimetableEntryDTO entry, Boolean conflict) {
            this.entry = entry;
            this.conflict = conflict;
        }
    }

    private boolean repairTeacherOverlapByClassSlotSwap(List<TimetableEntryDTO> entries, TimetableEntryDTO target) {
        if (target == null || target.getTeacherId() == null || isBlank(target.getClassName()) || isBlank(target.getSection())) return false;
        Long targetTeacherId = target.getTeacherId();
        String originalDay = normalizeDay(target.getDayOfWeek());
        Integer originalPeriod = target.getPeriodNumber();
        if (isBlank(originalDay) || originalPeriod == null) return false;

        List<TimetableEntryDTO> candidates = entries.stream()
                .filter(entry -> entry != target)
                .filter(entry -> equalsText(entry.getClassName(), target.getClassName()))
                .filter(entry -> equalsText(entry.getSection(), target.getSection()))
                .filter(entry -> !isBlank(entry.getDayOfWeek()))
                .filter(entry -> entry.getPeriodNumber() != null)
                .sorted(Comparator.comparing((TimetableEntryDTO entry) -> Math.abs(slotIndex(entry) - slotIndex(target)))
                        .thenComparing(entry -> safeText(entry.getSubjectName()))
                        .thenComparing(entry -> safeText(entry.getId())))
                .toList();

        for (TimetableEntryDTO candidate : candidates) {
            String candidateDay = normalizeDay(candidate.getDayOfWeek());
            Integer candidatePeriod = candidate.getPeriodNumber();
            Long candidateTeacherId = candidate.getTeacherId();
            if (equalsText(candidateDay, originalDay) && candidatePeriod.equals(originalPeriod)) continue;
            if (candidateTeacherId != null && candidateTeacherId.equals(targetTeacherId)) continue;

            if (!isTeacherFreeAt(entries, targetTeacherId, candidateDay, candidatePeriod, target, candidate)) continue;
            if (candidateTeacherId != null && !isTeacherFreeAt(entries, candidateTeacherId, originalDay, originalPeriod, target, candidate)) continue;

            int before = countBlockingConflicts(entries);
            swapSlots(target, candidate);
            int after = countBlockingConflicts(entries);
            if (after < before) return true;
            swapSlots(target, candidate);
        }
        return false;
    }

    private boolean repairClassSlotOverlapByEmptySlot(List<TimetableEntryDTO> entries, TimetableEntryDTO target) {
        if (target == null || isBlank(target.getClassName()) || isBlank(target.getSection()) || target.getTeacherId() == null) return false;
        int maxPeriod = maxPeriod(entries);
        for (String day : activeDays(entries)) {
            for (int period = 1; period <= maxPeriod; period++) {
                if (!isClassSectionFreeAt(entries, target.getClassName(), target.getSection(), day, period, target)) continue;
                if (!isTeacherFreeAt(entries, target.getTeacherId(), day, period, target, null)) continue;
                String oldDay = target.getDayOfWeek();
                Integer oldPeriod = target.getPeriodNumber();
                int before = countBlockingConflicts(entries);
                target.setDayOfWeek(day);
                target.setPeriodNumber(period);
                applyPeriodTimes(target, period);
                int after = countBlockingConflicts(entries);
                if (after < before) return true;
                target.setDayOfWeek(oldDay);
                target.setPeriodNumber(oldPeriod);
                applyPeriodTimes(target, oldPeriod);
            }
        }
        return false;
    }

    private int countBlockingConflicts(List<TimetableEntryDTO> entries) {
        return teacherOverlapGroups(entries).size() + classSectionSlotOverlapGroups(entries).size();
    }

    private List<List<TimetableEntryDTO>> teacherOverlapGroups(List<TimetableEntryDTO> entries) {
        Map<String, List<TimetableEntryDTO>> grouped = entries.stream()
                .filter(entry -> entry.getTeacherId() != null)
                .filter(entry -> !isBlank(entry.getDayOfWeek()))
                .filter(entry -> entry.getPeriodNumber() != null)
                .collect(Collectors.groupingBy(entry -> entry.getTeacherId() + "|" + normalizeDay(entry.getDayOfWeek()) + "|" + entry.getPeriodNumber(), LinkedHashMap::new, Collectors.toList()));
        return grouped.values().stream()
                .filter(group -> group.size() > 1)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<List<TimetableEntryDTO>> classSectionSlotOverlapGroups(List<TimetableEntryDTO> entries) {
        Map<String, List<TimetableEntryDTO>> grouped = entries.stream()
                .filter(entry -> !isBlank(entry.getClassName()))
                .filter(entry -> !isBlank(entry.getSection()))
                .filter(entry -> !isBlank(entry.getDayOfWeek()))
                .filter(entry -> entry.getPeriodNumber() != null)
                .collect(Collectors.groupingBy(entry -> safeText(entry.getClassName()) + "|" + safeText(entry.getSection()) + "|" + normalizeDay(entry.getDayOfWeek()) + "|" + entry.getPeriodNumber(), LinkedHashMap::new, Collectors.toList()));
        return grouped.values().stream()
                .filter(group -> group.size() > 1)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean isTeacherFreeAt(List<TimetableEntryDTO> entries, Long teacherId, String day, Integer period, TimetableEntryDTO firstIgnored, TimetableEntryDTO secondIgnored) {
        if (teacherId == null || isBlank(day) || period == null) return false;
        return entries.stream()
                .filter(entry -> entry != firstIgnored && entry != secondIgnored)
                .filter(entry -> teacherId.equals(entry.getTeacherId()))
                .noneMatch(entry -> equalsText(normalizeDay(entry.getDayOfWeek()), day) && period.equals(entry.getPeriodNumber()));
    }

    private boolean isClassSectionFreeAt(List<TimetableEntryDTO> entries, String className, String section, String day, Integer period, TimetableEntryDTO ignored) {
        return entries.stream()
                .filter(entry -> entry != ignored)
                .filter(entry -> equalsText(entry.getClassName(), className))
                .filter(entry -> equalsText(entry.getSection(), section))
                .noneMatch(entry -> equalsText(normalizeDay(entry.getDayOfWeek()), day) && period.equals(entry.getPeriodNumber()));
    }

    private void swapSlots(TimetableEntryDTO left, TimetableEntryDTO right) {
        String leftDay = left.getDayOfWeek();
        Integer leftPeriod = left.getPeriodNumber();
        String leftStart = left.getStartTime();
        String leftEnd = left.getEndTime();
        String leftRoom = left.getRoomNumber();

        left.setDayOfWeek(right.getDayOfWeek());
        left.setPeriodNumber(right.getPeriodNumber());
        left.setStartTime(right.getStartTime());
        left.setEndTime(right.getEndTime());
        left.setRoomNumber(right.getRoomNumber());

        right.setDayOfWeek(leftDay);
        right.setPeriodNumber(leftPeriod);
        right.setStartTime(leftStart);
        right.setEndTime(leftEnd);
        right.setRoomNumber(leftRoom);
    }

    private int slotIndex(TimetableEntryDTO entry) {
        int dayIndex = Math.max(0, activeDays(List.of(entry)).indexOf(normalizeDay(entry.getDayOfWeek())));
        int period = entry.getPeriodNumber() == null ? 0 : entry.getPeriodNumber();
        return dayIndex * 20 + period;
    }

    private List<String> activeDays(List<TimetableEntryDTO> entries) {
        LinkedHashSet<String> days = entries.stream()
                .map(entry -> normalizeDay(entry.getDayOfWeek()))
                .filter(day -> !isBlank(day))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (days.isEmpty()) days.addAll(DAYS);
        return new ArrayList<>(days);
    }

    private int maxPeriod(List<TimetableEntryDTO> entries) {
        return Math.max(PERIODS_PER_DAY, entries.stream()
                .filter(entry -> entry.getPeriodNumber() != null)
                .mapToInt(TimetableEntryDTO::getPeriodNumber)
                .max()
                .orElse(PERIODS_PER_DAY));
    }

    private void applyPeriodTimes(TimetableEntryDTO entry, Integer period) {
        if (entry == null || period == null) return;
        LocalTime start = LocalTime.of(9, 0).plusMinutes((long) (period - 1) * 45L);
        entry.setStartTime(start.format(TIME_FORMATTER));
        entry.setEndTime(start.plusMinutes(40).format(TIME_FORMATTER));
    }

    public TimetableGenerationResponseDTO openManualEdit(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        refreshBatch(batch);
        return batch;
    }

    public TimetableGenerationResponseDTO manualEdit(String batchId, TimetableManualEditRequestDTO request) {
        return manualEdit(batchId, request, "ADMIN", null);
    }

    public TimetableGenerationResponseDTO manualEdit(String batchId, TimetableManualEditRequestDTO request, String role, String editedBy) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        if (!isAdminRole(role)) {
            addVersion(batch.getGeneratedBatchId(), "SYSTEM", "MANUAL_EDIT_DENIED", batch.getEntries().size(), "Manual edit denied because only Admin/Principal can edit timetable batches.");
            return batch;
        }
        if (Boolean.TRUE.equals(publishLocks.get(batch.getGeneratedBatchId()))) {
            addVersion(batch.getGeneratedBatchId(), actorName(role, editedBy), "MANUAL_EDIT_BLOCKED", batch.getEntries().size(), "Published timetable is locked. Manual edit must operate on an unlocked batch version only.");
            return batch;
        }
        if (request == null || isBlank(request.getEntryId())) return batch;

        String actor = actorName(role, editedBy);
        String editNote = "Manual edit requested for entry " + request.getEntryId() + ".";
        for (TimetableEntryDTO entry : batch.getEntries()) {
            if (request.getEntryId().equals(String.valueOf(entry.getId()))) {
                List<String> changes = new ArrayList<>();
                if (!isBlank(request.getSubjectName()) && !equalsText(entry.getSubjectName(), request.getSubjectName())) {
                    changes.add("Subject: " + entry.getSubjectName() + " → " + request.getSubjectName().trim());
                    entry.setSubjectName(request.getSubjectName().trim());
                }
                if (request.getTeacherId() != null && !request.getTeacherId().equals(entry.getTeacherId())) {
                    changes.add("Teacher ID: " + entry.getTeacherId() + " → " + request.getTeacherId());
                    entry.setTeacherId(request.getTeacherId());
                }
                if (!isBlank(request.getTeacherName()) && !equalsText(entry.getTeacherName(), request.getTeacherName())) {
                    changes.add("Teacher: " + entry.getTeacherName() + " → " + request.getTeacherName().trim());
                    entry.setTeacherName(request.getTeacherName().trim());
                }
                if (!isBlank(request.getDayOfWeek()) && !equalsText(entry.getDayOfWeek(), request.getDayOfWeek())) {
                    changes.add("Day: " + entry.getDayOfWeek() + " → " + normalizeDay(request.getDayOfWeek()));
                    entry.setDayOfWeek(normalizeDay(request.getDayOfWeek()));
                }
                if (request.getPeriodNumber() != null && !request.getPeriodNumber().equals(entry.getPeriodNumber())) {
                    changes.add("Period: " + entry.getPeriodNumber() + " → " + request.getPeriodNumber());
                    entry.setPeriodNumber(request.getPeriodNumber());
                }
                if (!isBlank(request.getRoomNumber()) && !equalsText(entry.getRoomNumber(), request.getRoomNumber())) {
                    changes.add("Room: " + entry.getRoomNumber() + " → " + request.getRoomNumber().trim());
                    entry.setRoomNumber(request.getRoomNumber().trim());
                }
                if (!isBlank(request.getStartTime())) entry.setStartTime(request.getStartTime().trim());
                if (!isBlank(request.getEndTime())) entry.setEndTime(request.getEndTime().trim());
                entry.setIsLab("Computer".equalsIgnoreCase(entry.getSubjectName()) || "Science Lab".equalsIgnoreCase(entry.getSubjectName()));
                entry.setIsSports("Sports".equalsIgnoreCase(entry.getSubjectName()));
                editNote = changes.isEmpty() ? "Manual edit opened with no field changes for entry " + entry.getId() + "." : String.join("; ", changes);
                break;
            }
        }
        refreshBatch(batch);
        generatedBatches.put(batch.getGeneratedBatchId(), batch);
        syncExistingImportAfterRevalidation(batch, "MANUAL_EDIT_SAVED");
        addVersion(batch.getGeneratedBatchId(), actor, "MANUAL_EDIT_SAVED", batch.getEntries().size(), editNote + " Revalidated: " + batchStatus(batch) + " (" + batch.getConflictsDetected() + " conflict(s), " + batch.getCompletionPercentage() + "% ready).");
        return batch;
    }

    public TimetableGenerationResponseDTO revalidateBatch(String batchId, String role) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        if (!isAdminRole(role)) return batch;
        refreshBatch(batch);
        generatedBatches.put(batch.getGeneratedBatchId(), batch);
        syncExistingImportAfterRevalidation(batch, "REVALIDATED");
        addVersion(batch.getGeneratedBatchId(), actorName(role, null), "BATCH_REVALIDATED", batch.getEntries().size(), "Manual edit batch revalidated. Status: " + batchStatus(batch) + "; conflicts: " + batch.getConflictsDetected() + "; readiness: " + batch.getCompletionPercentage() + "%. Publish remains disabled until zero blocking conflicts.");
        return batch;
    }


    private String actorName(String role, String name) {
        if (!isBlank(name)) return name.trim();
        return "PRINCIPAL".equalsIgnoreCase(role) ? "Principal" : "Admin";
    }

    private String batchStatus(TimetableGenerationResponseDTO batch) {
        if (batch == null) return "REVIEW";
        if (Boolean.TRUE.equals(publishLocks.get(batch.getGeneratedBatchId())) && batch.getGeneratedBatchId().equals(latestPublishedBatchId)) return "PUBLISHED_ACTIVE";
        if (batch.getConflictsDetected() != null && batch.getConflictsDetected() > 0) return "NEEDS_CORRECTION";
        return batch.getCompletionPercentage() != null && batch.getCompletionPercentage() >= 100 ? "READY_TO_PUBLISH" : "REVIEW";
    }

    private void syncExistingImportAfterRevalidation(TimetableGenerationResponseDTO batch, String event) {
        if (batch == null || isBlank(batch.getGeneratedBatchId())) return;
        ExistingTimetableImportResponseDTO imported = existingTimetableImports.get(batch.getGeneratedBatchId());
        if (imported == null) return;
        int conflicts = batch.getConflictsDetected() == null ? 0 : batch.getConflictsDetected();
        imported.setPreviewEntries(batch.getEntries());
        imported.setConflicts(batch.getConflicts());
        imported.setConflictsDetected(conflicts);
        imported.setAcceptedRows(batch.getEntries() == null ? 0 : batch.getEntries().size());
        imported.setTotalPeriodAllocations(batch.getEntries() == null ? 0 : batch.getEntries().size());
        imported.setErrorCount(conflicts);
        imported.setValid(conflicts == 0);
        imported.setCanPublish(conflicts == 0);
        imported.setStatus(conflicts == 0 ? "READY_TO_PUBLISH" : "NEEDS_CORRECTION");
        imported.setMessage(conflicts == 0
                ? "Manual edits saved and revalidated. Imported timetable is ready to publish."
                : "Manual edits saved to the batch only. Blocking conflicts remain, so publish is disabled.");
        updateTimetableImportFileMetadataStatus(batch.getGeneratedBatchId(), imported.getStatus());
        if (conflicts == 0) {
            replaceLifecycleNotification(batch.getGeneratedBatchId(), "ADMIN_PRINCIPAL", "Timetable ready for publishing review", "Batch " + batch.getGeneratedBatchId() + " passed repair and revalidation. Publish after final Admin/Principal review.");
        } else {
            replaceLifecycleNotification(batch.getGeneratedBatchId(), "ADMIN_PRINCIPAL", "Existing timetable needs correction", "Open Timetable Operations with batch " + batch.getGeneratedBatchId() + " to review conflicts and corrections.");
        }
    }

    public TimetablePublishResponseDTO publish(String batchId) {
        return publish(batchId, null);
    }

    public TimetablePublishResponseDTO publish(String batchId, String approvedByName) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        refreshBatch(batch);
        int conflicts = batch.getConflicts() == null ? 0 : batch.getConflicts().size();
        int errors = Math.max(0, batch.getConflictsDetected() == null ? conflicts : batch.getConflictsDetected());
        String currentStatus = batchStatus(batch);
        boolean success = conflicts == 0 && errors == 0 && "READY_TO_PUBLISH".equals(currentStatus);
        String publishedAt = success ? LocalDateTime.now().toString() : null;
        String approvedBy = isBlank(approvedByName) ? "Principal/Admin" : approvedByName.trim();
        String notificationMessage = success
                ? "Final timetable is published. Teachers can follow the updated weekly schedule."
                : "Timetable publish blocked. Repair or manually edit conflicts before notifying teachers.";
        TimetablePublishResponseDTO response = new TimetablePublishResponseDTO(
                success,
                batch.getGeneratedBatchId(),
                success ? "PUBLISHED" : "PUBLISH_BLOCKED",
                success ? "Timetable published successfully for school operations." : "Publish blocked. Batch must be READY_TO_PUBLISH with zero errors and zero conflicts.",
                success ? batch.getEntries().size() : 0,
                conflicts,
                publishedAt,
                approvedBy,
                notificationMessage
        );
        if (success) {
            String schoolId = batchSchoolIds.getOrDefault(batch.getGeneratedBatchId(), "DEMO");
            String previousActiveBatchId = activePublishedBatchBySchool.get(schoolId);
            archivePreviousActiveTimetable(schoolId, batch.getGeneratedBatchId(), publishedAt, approvedBy);
            latestPublishedBatchId = batch.getGeneratedBatchId();
            activePublishedBatchBySchool.put(schoolId, batch.getGeneratedBatchId());
            publishLocks.put(batch.getGeneratedBatchId(), true);
            updateTimetableImportFileMetadataStatus(batch.getGeneratedBatchId(), "PUBLISHED");
            ExistingTimetableImportResponseDTO publishedImport = existingTimetableImports.get(batch.getGeneratedBatchId());
            if (publishedImport != null) {
                publishedImport.setPublishedBatchId(batch.getGeneratedBatchId());
                publishedImport.setStatus("PUBLISHED_ACTIVE");
                publishedImport.setCanPublish(true);
                publishedImport.setMessage("Final timetable is published and active. Visible to Teachers, Students, and Parents.");
            }
            addVersion(batch.getGeneratedBatchId(), approvedBy, "PUBLISHED_LOCKED", batch.getEntries().size(), "Published timetable locked as the one ACTIVE timetable for this school.");
            replaceLifecycleNotification(batch.getGeneratedBatchId(), "ADMIN_PRINCIPAL", "Timetable published active", "Batch " + batch.getGeneratedBatchId() + " is now the active published timetable for this school.");
            replaceLifecycleNotification(batch.getGeneratedBatchId(), "TEACHERS_STUDENTS_PARENTS", "New timetable published", notificationMessage);
            archiveHistory.put(batch.getGeneratedBatchId(), new TimetableArchiveSummaryDTO(batch.getGeneratedBatchId(), publishedAt, approvedBy, batch.getEntries().size(), "ACTIVE", "Current active published timetable for school " + schoolId + "."));
            TimetablePublishAuditDTO audit = new TimetablePublishAuditDTO(
                    "PUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    batch.getGeneratedBatchId(),
                    response.getStatus(),
                    publishedAt,
                    approvedBy,
                    response.getPublishedEntries(),
                    response.getRemainingConflicts(),
                    batch.getClassSectionReviews() == null ? 0 : batch.getClassSectionReviews().size(),
                    response.getMessage(),
                    previousActiveBatchId,
                    batch.getGeneratedBatchId(),
                    versions(batch.getGeneratedBatchId()).size(),
                    batch.getCompletionPercentage(),
                    errors
            );
            publishAudits.computeIfAbsent(batch.getGeneratedBatchId(), key -> new ArrayList<>()).add(0, audit);
        } else {
            TimetablePublishAuditDTO blockedAudit = new TimetablePublishAuditDTO(
                    "PUB-BLOCK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                    batch.getGeneratedBatchId(),
                    response.getStatus(),
                    null,
                    approvedBy,
                    0,
                    conflicts,
                    batch.getClassSectionReviews() == null ? 0 : batch.getClassSectionReviews().size(),
                    response.getMessage(),
                    activePublishedBatchBySchool.get(batchSchoolIds.getOrDefault(batch.getGeneratedBatchId(), "DEMO")),
                    activePublishedBatchBySchool.get(batchSchoolIds.getOrDefault(batch.getGeneratedBatchId(), "DEMO")),
                    versions(batch.getGeneratedBatchId()).size(),
                    batch.getCompletionPercentage(),
                    errors
            );
            publishAudits.computeIfAbsent(batch.getGeneratedBatchId(), key -> new ArrayList<>()).add(0, blockedAudit);
        }
        return response;
    }

    public List<TimetablePublishAuditDTO> publishHistory(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        return publishAudits.getOrDefault(batch.getGeneratedBatchId(), List.of());
    }


    public List<TimetableBatchSummaryDTO> listBatches() {
        List<TimetableBatchSummaryDTO> summaries = generatedBatches.values().stream()
                .map(this::toBatchSummary)
                .sorted(Comparator.comparingInt((TimetableBatchSummaryDTO summary) -> batchStatusSortRank(summary.getStatus()))
                        .thenComparing(summary -> summary.getUploadedAt() == null ? "" : summary.getUploadedAt(), Comparator.reverseOrder())
                        .thenComparing(TimetableBatchSummaryDTO::getBatchId, Comparator.reverseOrder()))
                .collect(Collectors.toCollection(ArrayList::new));

        if (summaries.isEmpty()) {
            TimetableGenerationResponseDTO fallback = findBatchOrCreateFallback(latestBatchId);
            refreshBatch(fallback);
            summaries.add(toBatchSummary(fallback));
        }
        return summaries;
    }

    private int batchStatusSortRank(String status) {
        if ("PUBLISHED_ACTIVE".equals(status)) return 0;
        if ("READY_TO_PUBLISH".equals(status)) return 1;
        if ("NEEDS_CORRECTION".equals(status)) return 2;
        if ("PUBLISH_BLOCKED".equals(status)) return 3;
        if ("ARCHIVED".equals(status)) return 4;
        return 5;
    }


    public TimetableBatchSummaryDTO batchSummary(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        refreshBatch(batch);
        return toBatchSummary(batch);
    }

    private TimetableBatchSummaryDTO toBatchSummary(TimetableGenerationResponseDTO batch) {
        refreshBatch(batch);
        List<TimetablePublishAuditDTO> audits = publishAudits.getOrDefault(batch.getGeneratedBatchId(), List.of());
        TimetablePublishAuditDTO latestAudit = audits.isEmpty() ? null : audits.get(0);
        boolean locked = Boolean.TRUE.equals(publishLocks.get(batch.getGeneratedBatchId()));
        boolean latestPublished = batch.getGeneratedBatchId().equals(latestPublishedBatchId);
        boolean archived = archiveHistory.containsKey(batch.getGeneratedBatchId()) && !latestPublished;
        String status;
        if (latestPublished && locked) {
            status = "PUBLISHED_ACTIVE";
        } else if (archived) {
            status = "ARCHIVED";
        } else if (latestAudit != null) {
            status = latestAudit.getStatus();
        } else if (batch.getConflictsDetected() != null && batch.getConflictsDetected() > 0) {
            status = "NEEDS_CORRECTION";
        } else {
            status = "READY_TO_PUBLISH";
        }
        String message = latestAudit != null
                ? latestAudit.getMessage()
                : (batch.getConflictsDetected() != null && batch.getConflictsDetected() > 0
                   ? "Batch has blocking conflicts. Use Auto Conflict Repair or Manual Edit before publishing."
                   : "Batch is ready for Admin/Principal publish review.");
        String uploadedAt = versions(batch.getGeneratedBatchId()).stream().reduce((first, second) -> second).map(TimetableVersionDTO::getCreatedAt).orElse(null);
        String uploadedBy = versions(batch.getGeneratedBatchId()).stream().reduce((first, second) -> second).map(version -> normalizeTimetableActor(version.getCreatedBy())).orElse("Admin");
        return new TimetableBatchSummaryDTO(
                batch.getGeneratedBatchId(),
                status,
                batch.getTotalEntries(),
                batch.getClassSectionReviews() == null ? 0 : batch.getClassSectionReviews().size(),
                batch.getConflictsDetected(),
                batch.getOverloadRiskTeachers(),
                batch.getCompletionPercentage(),
                latestAudit == null ? null : latestAudit.getPublishedAt(),
                latestAudit == null ? null : latestAudit.getApprovedBy(),
                message,
                uploadedAt,
                uploadedBy,
                locked,
                latestPublished,
                archived
        );
    }

    public TimetablePublishAuditDTO latestPublished() {
        if (latestPublishedBatchId != null) {
            List<TimetablePublishAuditDTO> audits = publishAudits.getOrDefault(latestPublishedBatchId, List.of());
            if (!audits.isEmpty()) return audits.get(0);
        }
        return new TimetablePublishAuditDTO("NOT-PUBLISHED", latestBatchId, "NOT_PUBLISHED", null, null, 0, 0, 0, "No timetable has been published in this server session yet.");
    }

    public TimetableExportResponseDTO export(String batchId, String format) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        String safeFormat = isBlank(format) ? "EXCEL" : format.trim().toUpperCase();
        StringBuilder content = new StringBuilder();
        content.append("Class,Section,Day,Period,Subject,Teacher,Room,Start,End\n");
        batch.getEntries().stream()
                .sorted(Comparator.comparing(TimetableEntryDTO::getClassName).thenComparing(TimetableEntryDTO::getSection).thenComparing(e -> DAYS.indexOf(e.getDayOfWeek())).thenComparing(TimetableEntryDTO::getPeriodNumber))
                .forEach(entry -> content.append(entry.getClassName()).append(',')
                        .append(entry.getSection()).append(',')
                        .append(entry.getDayOfWeek()).append(',')
                        .append(entry.getPeriodNumber()).append(',')
                        .append(entry.getSubjectName()).append(',')
                        .append(entry.getTeacherName()).append(',')
                        .append(entry.getRoomNumber()).append(',')
                        .append(entry.getStartTime()).append(',')
                        .append(entry.getEndTime()).append('\n'));
        String extension = "PDF".equals(safeFormat) ? "pdf-preview.txt" : "csv";
        String contentType = "PDF".equals(safeFormat) ? "text/plain" : "text/csv";
        return new TimetableExportResponseDTO(batch.getGeneratedBatchId(), safeFormat, "timetable-" + batch.getGeneratedBatchId() + "." + extension, contentType, content.toString());
    }

    public PrincipalTimetableIntelligenceDTO principalIntelligence(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        refreshBatch(batch);
        int conflicts = batch.getConflicts() == null ? 0 : batch.getConflicts().size();
        int high = (int) batch.getConflicts().stream().filter(c -> "HIGH".equalsIgnoreCase(c.getSeverity())).count();
        int overload = (int) batch.getWorkloadSummary().stream().filter(w -> !"Balanced".equalsIgnoreCase(w.getStatus())).count();
        int score = Math.max(0, 100 - high * 25 - (conflicts - high) * 8 - overload * 4);
        PrincipalTimetableIntelligenceDTO dto = new PrincipalTimetableIntelligenceDTO();
        dto.setBatchId(batch.getGeneratedBatchId());
        dto.setTotalEntries(batch.getEntries().size());
        dto.setClassSections(batch.getClassSectionReviews().size());
        dto.setConflicts(conflicts);
        dto.setHighRiskConflicts(high);
        dto.setOverloadRiskTeachers(overload);
        dto.setPublishReadinessScore(score);
        dto.setReadinessStatus(score >= 90 ? "READY_TO_PUBLISH" : score >= 70 ? "REVIEW_RECOMMENDED" : "NEEDS_REPAIR");
        List<String> insights = new ArrayList<>();
        insights.add(conflicts == 0 ? "No active timetable conflicts. Publish workflow can proceed." : conflicts + " conflicts need repair/review before final publishing.");
        insights.add(overload == 0 ? "Teacher workload is balanced across the selected timetable." : overload + " teachers require workload review before approval.");
        insights.add("Principal can use Auto Repair, Manual Editor, Export/Publish, and Day 16 publish audit history for operational rollout.");
        insights.add(latestPublishedBatchId != null && latestPublishedBatchId.equals(batch.getGeneratedBatchId()) ? "This batch is the latest published timetable." : "This batch is not yet marked as the latest published timetable.");
        dto.setInsights(insights);
        dto.setTopWorkloadRisks(batch.getWorkloadSummary().stream().limit(5).toList());
        return dto;
    }


    public TimetableLiveResponseDTO liveTimetable(String batchId, String role, Long teacherId, String className, String section) {
        return liveTimetable(batchId, role, teacherId, null, className, section);
    }

    public TimetableLiveResponseDTO liveTimetable(String batchId, String role, Long teacherId, String teacherName, String className, String section) {
        return liveTimetable(batchId, role, teacherId, teacherName, className, section, null);
    }

    public TimetableLiveResponseDTO liveTimetable(String batchId, String role, Long teacherId, String teacherName, String className, String section, String schoolId) {
        String safeRole = isBlank(role) ? "ADMIN" : role.trim().toUpperCase();
        String safeSchoolId = isBlank(schoolId) ? "DEMO" : schoolId.trim().toUpperCase();
        String publishedBatchId = resolveLatestPublishedBatchId(safeSchoolId);
        String targetBatchId = isBlank(batchId) ? publishedBatchId : batchId;

        if (isBlank(targetBatchId)) {
            return new TimetableLiveResponseDTO(null, safeRole, safeRole, false, false, "No published imported timetable is active yet. Existing timetable screens remain safe and hidden until publish.", List.of());
        }

        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(targetBatchId);
        refreshBatch(batch);
        boolean published = batch.getGeneratedBatchId().equals(publishedBatchId) && Boolean.TRUE.equals(publishLocks.get(batch.getGeneratedBatchId()));
        List<TimetableEntryDTO> filtered = new ArrayList<>(batch.getEntries());
        if ("TEACHER".equals(safeRole)) {
            if (teacherId != null) {
                filtered = filtered.stream().filter(e -> teacherId.equals(e.getTeacherId())).collect(Collectors.toList());
            } else if (!isBlank(teacherName)) {
                String safeTeacherName = teacherName.trim();
                filtered = filtered.stream()
                        .filter(e -> !isBlank(e.getTeacherName()) && safeTeacherName.equalsIgnoreCase(e.getTeacherName().trim()))
                        .collect(Collectors.toList());
            }
        } else if (("STUDENT".equals(safeRole) || "PARENT".equals(safeRole)) && !isBlank(className) && !isBlank(section)) {
            filtered = filtered.stream().filter(e -> className.equalsIgnoreCase(e.getClassName()) && section.equalsIgnoreCase(e.getSection())).collect(Collectors.toList());
        }
        filtered.sort(Comparator.comparing(TimetableEntryDTO::getDayOfWeek, Comparator.nullsLast(String::compareToIgnoreCase)).thenComparing(TimetableEntryDTO::getPeriodNumber, Comparator.nullsLast(Integer::compareTo)));
        String scope = "ADMIN".equals(safeRole) || "PRINCIPAL".equals(safeRole) ? "WHOLE_SCHOOL" : safeRole;
        if (!isAdminRole(safeRole) && !published) {
            return new TimetableLiveResponseDTO(batch.getGeneratedBatchId(), safeRole, scope, false, false, "Timetable is not published yet. Draft timetables are hidden for Teacher, Student, and Parent roles.", List.of());
        }
        String message = published ? "Latest published timetable loaded for " + safeRole + "." : "Draft timetable loaded for Admin/Principal validation. Publish and lock before Teacher/Student/Parent rollout.";
        return new TimetableLiveResponseDTO(batch.getGeneratedBatchId(), safeRole, scope, published, Boolean.TRUE.equals(publishLocks.get(batch.getGeneratedBatchId())), message, filtered);
    }

    public TimetablePublishResponseDTO publishLock(String batchId, String role, String approvedBy) {
        if (!isAdminRole(role)) {
            TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
            return new TimetablePublishResponseDTO(false, batch.getGeneratedBatchId(), "RBAC_DENIED", "Only Admin or Principal can publish-lock timetable.", 0, batch.getConflictsDetected(), null, approvedBy, "Publish lock denied by role validation.");
        }
        return publish(batchId, approvedBy);
    }

    public TimetableGenerationResponseDTO swapTimetableEntry(String batchId, TimetableManualEditRequestDTO request, String role) {
        if (!isAdminRole(role)) return findBatchOrCreateFallback(batchId);
        return manualEdit(batchId, request, role, null);
    }

    public TimetableBinaryExportDTO binaryExport(String batchId, String format) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        refreshBatch(batch);
        String safeFormat = isBlank(format) ? "EXCEL" : format.trim().toUpperCase();
        byte[] bytes;
        String fileName;
        String contentType;
        if ("PDF".equals(safeFormat)) {
            bytes = buildSimplePdf(batch);
            fileName = "vidyasetu-timetable-" + batch.getGeneratedBatchId() + ".pdf";
            contentType = "application/pdf";
        } else {
            bytes = buildExcelHtml(batch).getBytes(StandardCharsets.UTF_8);
            fileName = "vidyasetu-timetable-" + batch.getGeneratedBatchId() + ".xls";
            contentType = "application/vnd.ms-excel";
            safeFormat = "EXCEL";
        }
        return new TimetableBinaryExportDTO(batch.getGeneratedBatchId(), safeFormat, fileName, contentType, Base64.getEncoder().encodeToString(bytes), bytes.length, "Real downloadable " + safeFormat + " export generated as Base64 payload.");
    }

    public List<TimetableVersionDTO> versions(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        List<TimetableVersionDTO> versions = versionHistory.getOrDefault(batch.getGeneratedBatchId(), List.of());
        return versions.isEmpty() ? List.of(new TimetableVersionDTO(1, batch.getGeneratedBatchId(), LocalDateTime.now().toString(), "SYSTEM", "CURRENT", batch.getEntries().size(), "Current server-session timetable snapshot.")) : versions;
    }

    public TimetableVersionDTO rollback(String batchId, Integer versionNumber, String role) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        if (!isAdminRole(role)) {
            return new TimetableVersionDTO(versionNumber, batch.getGeneratedBatchId(), LocalDateTime.now().toString(), role, "RBAC_DENIED", batch.getEntries().size(), "Only Admin or Principal can rollback timetable versions.");
        }
        publishLocks.put(batch.getGeneratedBatchId(), false);
        TimetableVersionDTO version = addVersion(batch.getGeneratedBatchId(), role, "ROLLBACK_READY", batch.getEntries().size(), "Rollback marker created for version " + versionNumber + ". Timetable unlocked for review/edit before republish.");
        addNotification(batch.getGeneratedBatchId(), "ADMIN_PRINCIPAL", "Timetable rollback started", "Batch " + batch.getGeneratedBatchId() + " moved back to review mode.");
        return version;
    }

    public List<TimetablePublishAuditDTO> publishHistoryAll() {
        return publishAudits.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(TimetablePublishAuditDTO::getPublishedAt, Comparator.nullsLast(String::compareToIgnoreCase)).reversed())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public TimetablePublishAuditDTO rollbackToPublishedBatch(String batchId, String role, String approvedByName) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        refreshBatch(batch);
        String safeRole = isBlank(role) ? "ADMIN" : role.trim().toUpperCase();
        String approvedBy = isBlank(approvedByName) ? safeRole : approvedByName.trim();
        if (!isAdminRole(safeRole)) {
            return new TimetablePublishAuditDTO(
                    "RBAC-DENIED",
                    batch.getGeneratedBatchId(),
                    "RBAC_DENIED",
                    null,
                    approvedBy,
                    0,
                    batch.getConflictsDetected(),
                    batch.getClassSectionReviews() == null ? 0 : batch.getClassSectionReviews().size(),
                    "Only Admin or Principal can restore an archived published timetable.",
                    null,
                    null
            );
        }
        if (batch.getConflictsDetected() != null && batch.getConflictsDetected() > 0) {
            return new TimetablePublishAuditDTO(
                    "ROLLBACK-BLOCKED",
                    batch.getGeneratedBatchId(),
                    "BLOCKED_BY_CONFLICTS",
                    null,
                    approvedBy,
                    0,
                    batch.getConflictsDetected(),
                    batch.getClassSectionReviews() == null ? 0 : batch.getClassSectionReviews().size(),
                    "Rollback to active is blocked because this batch still has conflicts.",
                    null,
                    null
            );
        }
        String schoolId = batchSchoolIds.getOrDefault(batch.getGeneratedBatchId(), "DEMO");
        String restoredAt = LocalDateTime.now().toString();
        String previousActiveBatchId = activePublishedBatchBySchool.get(schoolId);
        archivePreviousActiveTimetable(schoolId, batch.getGeneratedBatchId(), restoredAt, approvedBy);
        latestPublishedBatchId = batch.getGeneratedBatchId();
        activePublishedBatchBySchool.put(schoolId, batch.getGeneratedBatchId());
        publishLocks.put(batch.getGeneratedBatchId(), true);
        updateTimetableImportFileMetadataStatus(batch.getGeneratedBatchId(), "PUBLISHED");
        TimetablePublishAuditDTO audit = new TimetablePublishAuditDTO(
                "ROLL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                batch.getGeneratedBatchId(),
                "ROLLBACK_ACTIVE",
                restoredAt,
                approvedBy,
                batch.getEntries().size(),
                0,
                batch.getClassSectionReviews() == null ? 0 : batch.getClassSectionReviews().size(),
                "Rollback completed. Previous published batch restored as the active timetable.",
                previousActiveBatchId,
                batch.getGeneratedBatchId(),
                versions(batch.getGeneratedBatchId()).size(),
                batch.getCompletionPercentage(),
                0
        );
        publishAudits.computeIfAbsent(batch.getGeneratedBatchId(), key -> new ArrayList<>()).add(0, audit);
        addVersion(batch.getGeneratedBatchId(), approvedBy, "ROLLBACK_ACTIVE", batch.getEntries().size(), "Archived published batch restored as the active school timetable.");
        addNotification(batch.getGeneratedBatchId(), "ADMIN_PRINCIPAL", "Timetable rollback completed", "Batch " + batch.getGeneratedBatchId() + " is now the active published timetable.");
        archiveHistory.put(batch.getGeneratedBatchId(), new TimetableArchiveSummaryDTO(batch.getGeneratedBatchId(), restoredAt, approvedBy, batch.getEntries().size(), "ACTIVE", "Current active published timetable for school " + schoolId + "."));
        return audit;
    }

    public List<TimetableNotificationDTO> notifications(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        String status = batchStatus(batch);
        return timetableNotifications.getOrDefault(batch.getGeneratedBatchId(), List.of()).stream()
                .filter(notification -> shouldExposeNotificationForStatus(notification, status))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<TimetableArchiveSummaryDTO> archives() {
        return new ArrayList<>(archiveHistory.values());
    }

    public TimetableRolloutReadinessDTO rolloutReadiness(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        refreshBatch(batch);
        boolean locked = Boolean.TRUE.equals(publishLocks.get(batch.getGeneratedBatchId()));
        boolean latest = batch.getGeneratedBatchId().equals(latestPublishedBatchId);
        int conflicts = batch.getConflictsDetected() == null ? 0 : batch.getConflictsDetected();
        int notificationsCount = notifications(batch.getGeneratedBatchId()).size();
        int versionsCount = versions(batch.getGeneratedBatchId()).size();
        int teacherVisible = (int) batch.getEntries().stream().filter(e -> e.getTeacherId() != null).count();
        int studentParentVisible = (int) batch.getEntries().stream().filter(e -> !isBlank(e.getClassName()) && !isBlank(e.getSection())).count();

        List<String> blockers = new ArrayList<>();
        if (!locked && !latest) blockers.add("Publish lock is not completed for this batch.");
        if (conflicts > 0) blockers.add(conflicts + " conflict(s) still need repair before rollout.");
        if (teacherVisible == 0) blockers.add("Teacher live timetable visibility has no mapped teacher entries.");
        if (studentParentVisible == 0) blockers.add("Student/parent class-section visibility has no mapped entries.");

        List<String> checks = new ArrayList<>();
        checks.add(locked || latest ? "Publish lock/live rollout gate is active." : "Batch is still in draft/review mode.");
        checks.add(teacherVisible + " teacher-visible entries available for teacher timetable view.");
        checks.add(studentParentVisible + " class-section entries available for student/parent timetable view.");
        checks.add(notificationsCount + " rollout notification(s) prepared for stakeholders.");
        checks.add(versionsCount + " version/rollback record(s) available for audit trail.");

        int score = 100;
        if (!locked && !latest) score -= 30;
        score -= Math.min(40, conflicts * 10);
        if (teacherVisible == 0) score -= 15;
        if (studentParentVisible == 0) score -= 15;
        score = Math.max(0, score);

        return new TimetableRolloutReadinessDTO(
                batch.getGeneratedBatchId(),
                blockers.isEmpty(),
                locked,
                latest,
                batch.getEntries().size(),
                teacherVisible,
                studentParentVisible,
                conflicts,
                notificationsCount,
                versionsCount,
                score,
                blockers,
                checks
        );
    }

    public Map<String, Object> day18Status(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("batchId", batch.getGeneratedBatchId());
        status.put("latestPublished", batch.getGeneratedBatchId().equals(latestPublishedBatchId));
        status.put("locked", Boolean.TRUE.equals(publishLocks.get(batch.getGeneratedBatchId())));
        status.put("versions", versions(batch.getGeneratedBatchId()).size());
        status.put("notifications", notifications(batch.getGeneratedBatchId()).size());
        status.put("archived", archiveHistory.containsKey(batch.getGeneratedBatchId()));
        status.put("entries", batch.getEntries().size());
        status.put("conflicts", batch.getConflictsDetected());
        return status;
    }


    public ExistingTimetableImportResponseDTO importExistingTimetable(MultipartFile file, String schoolId, String uploadedBy) {
        String safeSchoolId = isBlank(schoolId) ? "DEMO" : schoolId.trim().toUpperCase();
        String importBatchId = "IMP-TT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        List<ExistingTimetableImportIssueDTO> issues = new ArrayList<>();
        List<ExistingTimetableImportRowDTO> rows = new ArrayList<>();
        if (file == null || file.isEmpty()) {
            issues.add(new ExistingTimetableImportIssueDTO(0, "ERROR", "file", "Please upload an Excel .xlsx file with Class, Section, Day, Period, Subject, Teacher columns."));
            return buildImportResponse(importBatchId, safeSchoolId, rows, issues, List.of(), "VALIDATION_FAILED", null);
        }
        StoredFile storedFile = null;
        try {
            byte[] bytes = file.getBytes();
            storedFile = fileStorageService.uploadTimetableImport(safeSchoolId, file, bytes);
            try (InputStream inputStream = new ByteArrayInputStream(bytes); Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
                if (sheet == null) {
                    issues.add(new ExistingTimetableImportIssueDTO(0, "ERROR", "sheet", "Excel workbook does not contain a timetable sheet."));
                    return buildImportResponse(importBatchId, safeSchoolId, rows, issues, List.of(), "VALIDATION_FAILED", null);
                }
                DataFormatter formatter = new DataFormatter();
                Map<String, Integer> headers = readHeaders(sheet.getRow(0), formatter);
                List<String> required = List.of("class", "section", "day", "period", "subject", "teacher");
                for (String requiredHeader : required) {
                    if (!headers.containsKey(requiredHeader)) {
                        issues.add(new ExistingTimetableImportIssueDTO(1, "ERROR", requiredHeader, "Missing required column: " + titleCase(requiredHeader)));
                    }
                }
                if (!issues.isEmpty()) return buildImportResponse(importBatchId, safeSchoolId, rows, issues, List.of(), "VALIDATION_FAILED", null);
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    ExistingTimetableImportRowDTO dto = new ExistingTimetableImportRowDTO(
                            cleanCell(row, headers.get("class"), formatter),
                            cleanCell(row, headers.get("section"), formatter),
                            normalizeDay(cleanCell(row, headers.get("day"), formatter)),
                            parsePeriod(cleanCell(row, headers.get("period"), formatter)),
                            cleanCell(row, headers.get("subject"), formatter),
                            cleanCell(row, headers.get("teacher"), formatter)
                    );
                    if (isBlank(dto.getClassName()) && isBlank(dto.getSection()) && isBlank(dto.getDay()) && dto.getPeriod() == null && isBlank(dto.getSubject()) && isBlank(dto.getTeacher())) continue;
                    validateImportRow(dto, rowIndex + 1, issues);
                    rows.add(dto);
                }
            }
        } catch (Exception ex) {
            issues.add(new ExistingTimetableImportIssueDTO(0, "ERROR", "file", "Unable to read the Excel timetable file. Please upload a valid .xlsx workbook."));
        }
        addImportConsistencyIssues(rows, issues);
        List<TimetableEntryDTO> entries = buildImportedEntries(rows);
        TimetableGenerationRequestDTO request = new TimetableGenerationRequestDTO();
        request.setPreventConsecutiveLabsEnabled(false);
        List<TimetableConflictDTO> conflicts = detectConflicts(entries, request);
        conflicts.forEach(conflict -> issues.add(new ExistingTimetableImportIssueDTO(conflict.getPeriodNumber(), "ERROR", "Teacher/Class Conflict", "Teacher Conflicts", conflict.getTitle() + " - " + conflict.getDescription())));
        ExistingTimetableImportResponseDTO response = buildImportResponse(importBatchId, safeSchoolId, rows, issues, conflicts, conflicts.isEmpty() && issues.stream().noneMatch(i -> "ERROR".equalsIgnoreCase(i.getSeverity())) ? "VALIDATED" : "VALIDATION_FAILED", entries);
        attachStoredFileMetadata(response, storedFile);
        saveTimetableImportFileMetadata(safeSchoolId, uploadedBy, importBatchId, response.getStatus(), storedFile);
        existingTimetableImports.put(importBatchId, response);
        if (entries != null && !entries.isEmpty()) {
            TimetableGenerationResponseDTO batch = new TimetableGenerationResponseDTO();
            batch.setGeneratedBatchId(importBatchId);
            batch.setEntries(entries);
            refreshBatch(batch);
            generatedBatches.put(importBatchId, batch);
            batchSchoolIds.put(importBatchId, safeSchoolId);
            latestBatchId = importBatchId;
            String actor = isBlank(uploadedBy) ? "ADMIN" : uploadedBy;
            String event = Boolean.TRUE.equals(response.getCanPublish()) ? "EXISTING_TIMETABLE_IMPORTED" : "EXISTING_TIMETABLE_NEEDS_CORRECTION";
            String note = Boolean.TRUE.equals(response.getCanPublish())
                    ? "Existing school timetable imported from Excel and converted into VidyaSetu timetable format."
                    : "Existing timetable imported with blocking validation errors for Timetable Operations review.";
            addVersion(importBatchId, actor, event, entries.size(), note);
            replaceLifecycleNotification(importBatchId, "ADMIN_PRINCIPAL", Boolean.TRUE.equals(response.getCanPublish()) ? "Existing timetable validated" : "Existing timetable needs correction", Boolean.TRUE.equals(response.getCanPublish()) ? "Imported timetable is ready for Admin/Principal preview and publish." : "Open Timetable Operations with batch " + importBatchId + " to review conflicts and corrections.");
        }
        return response;
    }

    public ExistingTimetableImportStatusDTO existingTimetableImportStatus(String schoolId) {
        String safeSchoolId = isBlank(schoolId) ? "DEMO" : schoolId.trim().toUpperCase();
        String activeBatchId = resolveLatestPublishedBatchId(safeSchoolId);
        ExistingTimetableImportResponseDTO latest = existingTimetableImports.values().stream()
                .filter(item -> safeSchoolId.equalsIgnoreCase(item.getSchoolId()))
                .reduce((first, second) -> second)
                .orElse(null);
        if (!isBlank(activeBatchId) && existingTimetableImports.containsKey(activeBatchId)) {
            latest = existingTimetableImports.get(activeBatchId);
        }

        ExistingTimetableImportStatusDTO status = new ExistingTimetableImportStatusDTO();
        status.setSchoolId(safeSchoolId);
        if (latest == null) {
            status.setStatus("NO_TIMETABLE_IMPORTED");
            status.setLabel("No Timetable Imported");
            status.setMessage("Import the school’s active timetable before attendance rollout if the school is not generating a new timetable.");
            status.setTotalClasses(0);
            status.setTotalSections(0);
            status.setTotalTeachers(0);
            status.setTotalPeriodAllocations(0);
            return status;
        }

        String latestBatchId = latest.getImportBatchId();
        boolean isActivePublished = !isBlank(activeBatchId)
                && activeBatchId.equals(latestBatchId)
                && Boolean.TRUE.equals(publishLocks.get(activeBatchId));

        status.setImportBatchId(latestBatchId);
        status.setPublishedBatchId(isActivePublished ? activeBatchId : latest.getPublishedBatchId());
        status.setTotalClasses(latest.getTotalClasses());
        status.setTotalSections(latest.getTotalSections());
        status.setTotalTeachers(latest.getTotalTeachers());
        status.setTotalPeriodAllocations(latest.getTotalPeriodAllocations());

        if (isActivePublished || "PUBLISHED_ACTIVE".equalsIgnoreCase(latest.getStatus()) || "PUBLISHED".equalsIgnoreCase(latest.getStatus())) {
            status.setStatus("PUBLISHED_ACTIVE");
            status.setLabel("Published Active");
            status.setMessage("Final timetable is published and active. Visible to Teachers, Students, and Parents.");
        } else if (Boolean.TRUE.equals(latest.getCanPublish())) {
            status.setStatus("READY_TO_PUBLISH");
            status.setLabel("Imported – Ready to Publish");
            status.setMessage("Imported timetable validation is complete. Admin/Principal can publish after review.");
        } else {
            status.setStatus("VALIDATION_FAILED");
            status.setLabel("Imported – Validation Failed");
            status.setMessage("Imported timetable requires correction before publish. Use the import batch ID in Timetable Operations for repair/manual review.");
        }
        return status;
    }

    public ExistingTimetableImportResponseDTO publishImportedTimetable(String importBatchId, String role, String approvedBy) {
        ExistingTimetableImportResponseDTO response = existingTimetableImports.get(importBatchId);
        if (response == null) {
            ExistingTimetableImportResponseDTO missing = new ExistingTimetableImportResponseDTO();
            missing.setImportBatchId(importBatchId);
            missing.setStatus("NOT_FOUND");
            missing.setValid(false);
            missing.setCanPublish(false);
            missing.setMessage("Imported timetable batch was not found. Please upload and validate the Excel timetable again.");
            return missing;
        }
        if (!isAdminRole(role)) {
            response.setStatus("RBAC_DENIED");
            response.setMessage("Only Admin or Principal can publish an imported timetable.");
            return response;
        }
        if (!Boolean.TRUE.equals(response.getCanPublish())) {
            response.setStatus("PUBLISH_BLOCKED");
            response.setMessage("Imported timetable has validation errors or conflicts. Fix them before publishing.");
            return response;
        }
        TimetablePublishResponseDTO publish = publishLock(importBatchId, role, approvedBy);
        response.setPublishedBatchId(publish.getBatchId());
        response.setStatus(Boolean.TRUE.equals(publish.getSuccess()) ? "PUBLISHED" : "PUBLISH_BLOCKED");
        response.setMessage(publish.getMessage());
        addNotification(importBatchId, "TEACHER", "Timetable Published", "Your latest teaching timetable is now available in My Timetable.");
        addNotification(importBatchId, "STUDENT", "Timetable Published", "Your latest class schedule is now available.");
        addNotification(importBatchId, "PARENT", "Timetable Published", "Your child\'s latest timetable is now available.");
        return response;
    }

    public List<TimetableNotificationDTO> roleNotifications(String role) {
        String safeRole = isBlank(role) ? "ADMIN_PRINCIPAL" : role.trim().toUpperCase();
        String publishedBatchId = resolveLatestPublishedBatchId();
        if (publishedBatchId == null) return List.of();
        return notifications(publishedBatchId).stream()
                .filter(n -> "ALL".equalsIgnoreCase(n.getAudience()) || safeRole.equalsIgnoreCase(n.getAudience()) || (isAdminRole(safeRole) && "ADMIN_PRINCIPAL".equalsIgnoreCase(n.getAudience())))
                .toList();
    }

    private String resolveLatestPublishedBatchId() {
        return resolveLatestPublishedBatchId(null);
    }

    private String resolveLatestPublishedBatchId(String schoolId) {
        String safeSchoolId = isBlank(schoolId) ? null : schoolId.trim().toUpperCase();
        if (safeSchoolId != null) {
            String activeForSchool = activePublishedBatchBySchool.get(safeSchoolId);
            if (!isBlank(activeForSchool) && generatedBatches.containsKey(activeForSchool) && Boolean.TRUE.equals(publishLocks.get(activeForSchool))) {
                return activeForSchool;
            }
        }
        if (!isBlank(latestPublishedBatchId) && generatedBatches.containsKey(latestPublishedBatchId) && Boolean.TRUE.equals(publishLocks.get(latestPublishedBatchId))) {
            return latestPublishedBatchId;
        }
        return publishLocks.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()) && generatedBatches.containsKey(entry.getKey()))
                .map(Map.Entry::getKey)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private void archivePreviousActiveTimetable(String schoolId, String newBatchId, String archivedAt, String approvedBy) {
        String safeSchoolId = isBlank(schoolId) ? "DEMO" : schoolId.trim().toUpperCase();
        String previousBatchId = activePublishedBatchBySchool.get(safeSchoolId);
        if (isBlank(previousBatchId) || previousBatchId.equals(newBatchId)) return;
        publishLocks.put(previousBatchId, false);
        TimetableGenerationResponseDTO previous = generatedBatches.get(previousBatchId);
        int entries = previous == null || previous.getEntries() == null ? 0 : previous.getEntries().size();
        archiveHistory.put(previousBatchId, new TimetableArchiveSummaryDTO(previousBatchId, archivedAt, approvedBy, entries, "ARCHIVED", "Archived automatically because a newer timetable is ACTIVE for school " + safeSchoolId + "."));
        updateTimetableImportFileMetadataStatus(previousBatchId, "ARCHIVED");
        ExistingTimetableImportResponseDTO previousImport = existingTimetableImports.get(previousBatchId);
        if (previousImport != null) {
            previousImport.setStatus("ARCHIVED");
            previousImport.setMessage("Archived automatically because a newer imported timetable was published.");
        }
    }

    public List<TimetableEntryDTO> activePublishedPeriods(String schoolId, String role, Long teacherId, String teacherName, String className, String section) {
        TimetableLiveResponseDTO live = liveTimetable(null, role, teacherId, teacherName, className, section, schoolId);
        return live.getEntries() == null ? List.of() : live.getEntries();
    }

    private void attachStoredFileMetadata(ExistingTimetableImportResponseDTO response, StoredFile storedFile) {
        if (response == null || storedFile == null) {
            return;
        }
        response.setFileStorageKey(storedFile.storageKey());
        response.setOriginalFilename(storedFile.originalFilename());
        response.setContentType(storedFile.contentType());
        response.setFileSizeBytes(storedFile.sizeBytes());
    }

    private void saveTimetableImportFileMetadata(String schoolId, String uploadedBy, String importBatchId, String status, StoredFile storedFile) {
        if (storedFile == null) {
            return;
        }
        TimetableImportFileMetadata metadata = new TimetableImportFileMetadata();
        metadata.setSchoolId(schoolId);
        metadata.setAcademicYear(null);
        metadata.setOriginalFilename(storedFile.originalFilename());
        metadata.setStorageKey(storedFile.storageKey());
        metadata.setContentType(storedFile.contentType());
        metadata.setFileSizeBytes(storedFile.sizeBytes());
        metadata.setUploadedBy(isBlank(uploadedBy) ? "ADMIN" : uploadedBy.trim());
        metadata.setUploadedAt(LocalDateTime.now());
        metadata.setStatus(status == null || status.isBlank() ? "UPLOADED" : status);
        metadata.setImportBatchId(importBatchId);
        timetableImportFileMetadataRepository.save(metadata);
    }


    private void updateTimetableImportFileMetadataStatus(String importBatchId, String status) {
        if (isBlank(importBatchId) || isBlank(status)) {
            return;
        }
        timetableImportFileMetadataRepository.findTopByImportBatchIdOrderByUploadedAtDesc(importBatchId)
                .ifPresent(metadata -> {
                    metadata.setStatus(status.trim().toUpperCase());
                    timetableImportFileMetadataRepository.save(metadata);
                });
    }

    private ExistingTimetableImportResponseDTO buildImportResponse(String importBatchId, String schoolId, List<ExistingTimetableImportRowDTO> rows, List<ExistingTimetableImportIssueDTO> issues, List<TimetableConflictDTO> conflicts, String status, List<TimetableEntryDTO> entries) {
        int errors = (int) issues.stream().filter(i -> "ERROR".equalsIgnoreCase(i.getSeverity())).count();
        int warnings = (int) issues.stream().filter(i -> "WARNING".equalsIgnoreCase(i.getSeverity())).count();
        ExistingTimetableImportResponseDTO response = new ExistingTimetableImportResponseDTO();
        response.setImportBatchId(importBatchId);
        response.setSchoolId(schoolId);
        response.setStatus(status);
        response.setRows(rows);
        response.setIssues(issues);
        response.setConflicts(conflicts);
        response.setPreviewEntries(entries == null ? List.of() : entries);
        response.setTotalRows(rows.size());
        response.setAcceptedRows(entries == null ? 0 : entries.size());
        response.setTotalClasses((int) rows.stream().map(ExistingTimetableImportRowDTO::getClassName).filter(value -> !isBlank(value)).map(String::trim).distinct().count());
        response.setTotalSections((int) rows.stream().map(row -> (safeText(row.getClassName()) + "-" + safeText(row.getSection())).toUpperCase()).filter(value -> !value.equals("-")).distinct().count());
        response.setTotalTeachers((int) rows.stream().map(ExistingTimetableImportRowDTO::getTeacher).filter(value -> !isBlank(value)).map(value -> value.trim().toUpperCase()).distinct().count());
        response.setTotalPeriodAllocations(entries == null ? 0 : entries.size());
        response.setErrorCount(errors);
        response.setWarningCount(warnings);
        response.setConflictsDetected(conflicts == null ? 0 : conflicts.size());
        response.setValidationCards(buildImportValidationCards(issues));
        response.setValid(errors == 0 && (conflicts == null || conflicts.isEmpty()) && !rows.isEmpty());
        response.setCanPublish(Boolean.TRUE.equals(response.getValid()));
        response.setMessage(Boolean.TRUE.equals(response.getValid()) ? "Existing timetable validated and converted into VidyaSetu timetable format. Review preview, then publish." : "Existing timetable needs correction before publish.");
        return response;
    }

    private Map<String, Integer> readHeaders(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> headers = new LinkedHashMap<>();
        if (headerRow == null) return headers;
        for (Cell cell : headerRow) {
            String key = formatter.formatCellValue(cell).trim().toLowerCase().replace(" ", "");
            if ("classname".equals(key)) key = "class";
            if ("dayofweek".equals(key)) key = "day";
            if ("periodnumber".equals(key)) key = "period";
            if ("subjectname".equals(key)) key = "subject";
            if ("teachername".equals(key)) key = "teacher";
            headers.put(key, cell.getColumnIndex());
        }
        return headers;
    }

    private String cleanCell(Row row, Integer index, DataFormatter formatter) {
        if (row == null || index == null) return "";
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    private Integer parsePeriod(String value) {
        if (isBlank(value)) return null;
        try { return (int) Math.round(Double.parseDouble(value.replaceAll("[^0-9.]", ""))); } catch (Exception ignored) { return null; }
    }

    private void validateImportRow(ExistingTimetableImportRowDTO row, int rowNumber, List<ExistingTimetableImportIssueDTO> issues) {
        if (isBlank(row.getClassName())) issues.add(new ExistingTimetableImportIssueDTO(rowNumber, "ERROR", "Class", "Missing Class", "Class is required."));
        if (isBlank(row.getSection())) issues.add(new ExistingTimetableImportIssueDTO(rowNumber, "ERROR", "Section", "Missing Section", "Section is required."));
        if (isBlank(row.getDay()) || !DAYS.contains(row.getDay())) issues.add(new ExistingTimetableImportIssueDTO(rowNumber, "ERROR", "Day", "Invalid Day", "Day must be Monday to Saturday."));
        if (row.getPeriod() == null || row.getPeriod() < 1 || row.getPeriod() > 12) issues.add(new ExistingTimetableImportIssueDTO(rowNumber, "ERROR", "Period", "Invalid Period", "Period must be a number between 1 and 12."));
        if (isBlank(row.getSubject())) issues.add(new ExistingTimetableImportIssueDTO(rowNumber, "ERROR", "Subject", "Missing Subject", "Subject is required."));
        if (isBlank(row.getTeacher())) issues.add(new ExistingTimetableImportIssueDTO(rowNumber, "ERROR", "Teacher", "Missing Teacher", "Teacher is required."));
    }


    private void addImportConsistencyIssues(List<ExistingTimetableImportRowDTO> rows, List<ExistingTimetableImportIssueDTO> issues) {
        Set<String> classSlots = new HashSet<>();
        Set<String> teacherSlots = new HashSet<>();
        Set<String> subjects = rows.stream().map(ExistingTimetableImportRowDTO::getSubject).filter(value -> !isBlank(value)).map(value -> value.trim().toUpperCase()).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> teachers = rows.stream().map(ExistingTimetableImportRowDTO::getTeacher).filter(value -> !isBlank(value)).map(value -> value.trim().toUpperCase()).collect(Collectors.toCollection(LinkedHashSet::new));
        for (int index = 0; index < rows.size(); index++) {
            ExistingTimetableImportRowDTO row = rows.get(index);
            int rowNumber = index + 2;
            if (!isBlank(row.getClassName()) && !isBlank(row.getSection()) && !isBlank(row.getDay()) && row.getPeriod() != null) {
                String classKey = safeText(row.getClassName()) + "|" + safeText(row.getSection()) + "|" + safeText(row.getDay()) + "|" + row.getPeriod();
                if (!classSlots.add(classKey)) {
                    issues.add(new ExistingTimetableImportIssueDTO(rowNumber, "ERROR", "Class Slot", "Class Conflicts", "This class section already has a timetable entry for the same day and period."));
                }
            }
            if (!isBlank(row.getTeacher()) && !isBlank(row.getDay()) && row.getPeriod() != null) {
                String teacherKey = safeText(row.getTeacher()) + "|" + safeText(row.getDay()) + "|" + row.getPeriod();
                if (!teacherSlots.add(teacherKey)) {
                    issues.add(new ExistingTimetableImportIssueDTO(rowNumber, "ERROR", "Teacher Slot", "Teacher Conflicts", "This teacher is assigned to more than one class in the same day and period."));
                }
            }
        }
        subjects.stream().filter(subject -> subject.length() < 2).forEach(subject -> issues.add(new ExistingTimetableImportIssueDTO(0, "WARNING", "Subject", "Unknown Subjects", "Review subject name before publishing.")));
        teachers.stream().filter(teacher -> teacher.length() < 2).forEach(teacher -> issues.add(new ExistingTimetableImportIssueDTO(0, "WARNING", "Teacher", "Unknown Teachers", "Review teacher name before publishing.")));
    }

    private List<ExistingTimetableImportSummaryDTO> buildImportValidationCards(List<ExistingTimetableImportIssueDTO> issues) {
        Map<String, List<ExistingTimetableImportIssueDTO>> grouped = issues.stream().collect(Collectors.groupingBy(issue -> isBlank(issue.getCategory()) ? safeText(issue.getFieldName()) : issue.getCategory(), LinkedHashMap::new, Collectors.toList()));
        List<ExistingTimetableImportSummaryDTO> cards = new ArrayList<>();
        for (Map.Entry<String, List<ExistingTimetableImportIssueDTO>> item : grouped.entrySet()) {
            boolean hasError = item.getValue().stream().anyMatch(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity()));
            cards.add(new ExistingTimetableImportSummaryDTO(item.getKey(), item.getValue().size(), hasError ? "ERROR" : "WARNING", importGuidance(item.getKey())));
        }
        return cards;
    }

    private String importGuidance(String category) {
        String value = safeText(category).toUpperCase();
        if (value.contains("CLASS CONFLICT")) return "A class section can have only one subject in a day-period slot.";
        if (value.contains("TEACHER CONFLICT")) return "A teacher can teach only one class in a day-period slot.";
        if (value.contains("PERIOD")) return "Use valid period numbers from the school timetable day.";
        if (value.contains("TEACHER")) return "Use teacher names that match the committed school workbook.";
        if (value.contains("SUBJECT")) return "Use subject names that match the committed school workbook.";
        if (value.contains("SECTION")) return "Use sections that exist for the selected class.";
        if (value.contains("CLASS")) return "Use classes that exist in the committed school workbook.";
        return "Update the Excel timetable and validate again.";
    }

    private List<TimetableEntryDTO> buildImportedEntries(List<ExistingTimetableImportRowDTO> rows) {
        List<TimetableEntryDTO> entries = new ArrayList<>();
        int sequence = 1;
        for (ExistingTimetableImportRowDTO row : rows) {
            if (isBlank(row.getClassName()) || isBlank(row.getSection()) || isBlank(row.getDay()) || row.getPeriod() == null || isBlank(row.getSubject()) || isBlank(row.getTeacher())) continue;
            LocalTime start = LocalTime.of(9, 0).plusMinutes((long) (row.getPeriod() - 1) * 45);
            entries.add(new TimetableEntryDTO(
                    "IMP-" + sequence++,
                    row.getClassName().trim(),
                    row.getSection().trim().toUpperCase(),
                    row.getSubject().trim(),
                    stableTeacherId(row.getTeacher()),
                    row.getTeacher().trim(),
                    row.getDay(),
                    row.getPeriod(),
                    "R-" + row.getClassName().trim() + row.getSection().trim().toUpperCase(),
                    start.format(TIME_FORMATTER),
                    start.plusMinutes(40).format(TIME_FORMATTER),
                    row.getSubject().toLowerCase().contains("lab") || row.getSubject().toLowerCase().contains("computer"),
                    row.getSubject().toLowerCase().contains("sport"),
                    false
            ));
        }
        return entries;
    }

    private Long stableTeacherId(String teacherName) {
        return 1000L + Math.abs((long) teacherName.trim().toLowerCase().hashCode() % 9000L);
    }

    private String normalizeDay(String value) {
        if (isBlank(value)) return "";
        String v = value.trim().toUpperCase();
        if (v.startsWith("MON")) return "MONDAY";
        if (v.startsWith("TUE")) return "TUESDAY";
        if (v.startsWith("WED")) return "WEDNESDAY";
        if (v.startsWith("THU")) return "THURSDAY";
        if (v.startsWith("FRI")) return "FRIDAY";
        if (v.startsWith("SAT")) return "SATURDAY";
        return v;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String titleCase(String value) {
        return value == null || value.isBlank() ? "" : value.substring(0, 1).toUpperCase() + value.substring(1);
    }


    private boolean isAdminRole(String role) {
        String safeRole = isBlank(role) ? "ADMIN" : role.trim().toUpperCase();
        return "ADMIN".equals(safeRole) || "PRINCIPAL".equals(safeRole);
    }

    private TimetableVersionDTO addVersion(String batchId, String createdBy, String changeType, Integer entriesCount, String notes) {
        String safeBatchId = isBlank(batchId) ? "UNKNOWN" : batchId;
        List<TimetableVersionDTO> versions = versionHistory.computeIfAbsent(safeBatchId, key -> new ArrayList<>());
        TimetableVersionDTO version = new TimetableVersionDTO(versions.size() + 1, safeBatchId, LocalDateTime.now().toString(), isBlank(createdBy) ? "SYSTEM" : createdBy, changeType, entriesCount, notes);
        versions.add(0, version);
        return version;
    }

    private String normalizeTimetableActor(String actor) {
        if (isBlank(actor) || "SYSTEM".equalsIgnoreCase(actor)) return "Admin";
        if ("ADMIN".equalsIgnoreCase(actor)) return "Admin";
        if ("PRINCIPAL".equalsIgnoreCase(actor)) return "Principal";
        return actor.trim();
    }

    private boolean isLifecycleNotificationTitle(String title) {
        if (isBlank(title)) return false;
        String safeTitle = title.trim().toLowerCase();
        return safeTitle.contains("existing timetable")
                || safeTitle.contains("timetable ready")
                || safeTitle.contains("timetable published")
                || safeTitle.contains("new timetable published")
                || safeTitle.contains("rollback completed");
    }

    private boolean shouldExposeNotificationForStatus(TimetableNotificationDTO notification, String status) {
        if (notification == null) return false;
        String title = (notification.getTitle() == null ? "" : notification.getTitle()).toLowerCase();
        String message = (notification.getMessage() == null ? "" : notification.getMessage()).toLowerCase();
        String combined = title + " " + message;
        if (("READY_TO_PUBLISH".equals(status) || "PUBLISHED_ACTIVE".equals(status) || "ARCHIVED".equals(status)) && combined.contains("needs correction")) return false;
        if ("PUBLISHED_ACTIVE".equals(status) && combined.contains("ready for publishing")) return false;
        return true;
    }

    private void replaceLifecycleNotification(String batchId, String audience, String title, String message) {
        String safeBatchId = isBlank(batchId) ? "UNKNOWN" : batchId;
        String safeAudience = isBlank(audience) ? "ADMIN_PRINCIPAL" : audience;
        List<TimetableNotificationDTO> notifications = timetableNotifications.computeIfAbsent(safeBatchId, key -> new ArrayList<>());
        notifications.removeIf(notification -> safeAudience.equalsIgnoreCase(notification.getAudience()) && isLifecycleNotificationTitle(notification.getTitle()));
        notifications.add(0, new TimetableNotificationDTO(
                "TTN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                safeBatchId,
                safeAudience,
                title,
                message,
                LocalDateTime.now().toString()
        ));
    }

    private void addNotification(String batchId, String audience, String title, String message) {
        timetableNotifications.computeIfAbsent(batchId, key -> new ArrayList<>()).add(0, new TimetableNotificationDTO(
                "TTN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                batchId,
                audience,
                title,
                message,
                LocalDateTime.now().toString()
        ));
    }

    private String buildExcelHtml(TimetableGenerationResponseDTO batch) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<h2>VidyaSetu Timetable - ").append(batch.getGeneratedBatchId()).append("</h2>");
        html.append("<table border='1'><tr><th>Class</th><th>Section</th><th>Day</th><th>Period</th><th>Subject</th><th>Teacher</th><th>Room</th><th>Start</th><th>End</th></tr>");
        batch.getEntries().stream()
                .sorted(Comparator.comparing(TimetableEntryDTO::getClassName).thenComparing(TimetableEntryDTO::getSection).thenComparing(e -> DAYS.indexOf(e.getDayOfWeek())).thenComparing(TimetableEntryDTO::getPeriodNumber))
                .forEach(e -> html.append("<tr><td>").append(escapeHtml(e.getClassName())).append("</td><td>")
                        .append(escapeHtml(e.getSection())).append("</td><td>").append(escapeHtml(e.getDayOfWeek())).append("</td><td>")
                        .append(e.getPeriodNumber()).append("</td><td>").append(escapeHtml(e.getSubjectName())).append("</td><td>")
                        .append(escapeHtml(e.getTeacherName())).append("</td><td>").append(escapeHtml(e.getRoomNumber())).append("</td><td>")
                        .append(escapeHtml(e.getStartTime())).append("</td><td>").append(escapeHtml(e.getEndTime())).append("</td></tr>"));
        html.append("</table></body></html>");
        return html.toString();
    }

    private byte[] buildSimplePdf(TimetableGenerationResponseDTO batch) {
        StringBuilder text = new StringBuilder();
        text.append("VidyaSetu Timetable ").append(batch.getGeneratedBatchId()).append("\\n");
        batch.getEntries().stream().limit(60).forEach(e -> text.append(e.getClassName()).append(e.getSection()).append(" ")
                .append(e.getDayOfWeek()).append(" P").append(e.getPeriodNumber()).append(" ")
                .append(e.getSubjectName()).append(" - ").append(e.getTeacherName()).append("\\n"));
        String stream = "BT /F1 10 Tf 40 780 Td " + pdfText(text.toString()) + " ET";
        String obj1 = "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n";
        String obj2 = "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n";
        String obj3 = "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n";
        String obj4 = "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n";
        String obj5 = "5 0 obj << /Length " + stream.getBytes(StandardCharsets.UTF_8).length + " >> stream\n" + stream + "\nendstream endobj\n";
        String body = obj1 + obj2 + obj3 + obj4 + obj5;
        String pdf = "%PDF-1.4\n" + body + "trailer << /Root 1 0 R >>\n%%EOF";
        return pdf.getBytes(StandardCharsets.UTF_8);
    }

    private String pdfText(String raw) {
        String[] lines = raw.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").split("\\n");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) out.append(" T* ");
            out.append("(").append(lines[i]).append(") Tj");
        }
        return out.toString();
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private boolean equalsText(String left, String right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.equalsIgnoreCase(right);
    }

    private void refreshBatch(TimetableGenerationResponseDTO batch) {
        batch.getEntries().forEach(entry -> entry.setConflict(false));
        TimetableGenerationRequestDTO request = new TimetableGenerationRequestDTO();
        request.setPreventConsecutiveLabsEnabled(true);
        List<TimetableConflictDTO> conflicts = detectConflicts(batch.getEntries(), request);
        List<TeacherWorkloadSummaryDTO> workload = buildWorkloadSummary(batch.getEntries());
        batch.setConflicts(conflicts);
        batch.setWorkloadSummary(workload);
        batch.setClassSectionReviews(buildClassSectionReviews(batch.getEntries()));
        batch.setTotalEntries(batch.getEntries().size());
        batch.setConflictsDetected(conflicts.size());
        batch.setOverloadRiskTeachers((int) workload.stream().filter(item -> !"Balanced".equalsIgnoreCase(item.getStatus())).count());
        batch.setCompletionPercentage(conflicts.isEmpty() ? 100 : Math.max(70, 100 - Math.min(25, conflicts.size() * 5)));
    }

    private TimetableGenerationResponseDTO findBatchOrCreateFallback(String batchId) {
        if (batchId != null && generatedBatches.containsKey(batchId)) return generatedBatches.get(batchId);
        if (latestBatchId != null && generatedBatches.containsKey(latestBatchId)) return generatedBatches.get(latestBatchId);

        TimetableGenerationRequestDTO fallback = new TimetableGenerationRequestDTO();
        fallback.setClassNames(List.of("1", "2"));
        fallback.setSections(List.of("A", "B"));
        fallback.setAutoDefaultTeacherPoolEnabled(true);
        fallback.setAutoLoadSectionsEnabled(true);
        fallback.setEqualDistributionEnabled(true);
        fallback.setWorkloadBalancingEnabled(true);
        fallback.setFixedLabPeriodsEnabled(true);
        fallback.setPreventConsecutiveLabsEnabled(true);
        fallback.setSameTeacherContinuityEnabled(true);
        fallback.setAvoidTeacherGapsEnabled(true);
        fallback.setAcademicRulesEngineEnabled(true);
        fallback.setAcademicRules(defaultAcademicRulesForClasses(fallback.getClassNames()));
        return generate(fallback);
    }

    private List<TeacherWorkloadSummaryDTO> buildWorkloadSummary(List<TimetableEntryDTO> entries) {
        Map<Long, List<TimetableEntryDTO>> grouped = entries.stream()
                .filter(entry -> entry.getTeacherId() != null)
                .collect(Collectors.groupingBy(TimetableEntryDTO::getTeacherId, LinkedHashMap::new, Collectors.toList()));
        List<TeacherWorkloadSummaryDTO> summary = new ArrayList<>();
        for (Map.Entry<Long, List<TimetableEntryDTO>> item : grouped.entrySet()) {
            int weeklyPeriods = item.getValue().size();
            int continuousRisk = calculateContinuousRisk(item.getValue());
            int freeGaps = Math.max(0, DAYS.size() * PERIODS_PER_DAY - weeklyPeriods);
            int score = Math.min(100, Math.max(10, weeklyPeriods * 2 + continuousRisk * 6));
            String status = score >= 80 ? "Overload" : score >= 55 ? "Watch" : "Balanced";
            summary.add(new TeacherWorkloadSummaryDTO(
                    item.getKey(),
                    item.getValue().get(0).getTeacherName(),
                    weeklyPeriods,
                    0,
                    continuousRisk,
                    freeGaps,
                    score,
                    status
            ));
        }
        return summary.stream()
                .sorted(Comparator.comparing(TeacherWorkloadSummaryDTO::getOverloadRiskScore).reversed())
                .toList();
    }

    private int calculateContinuousRisk(List<TimetableEntryDTO> teacherEntries) {
        Map<String, List<TimetableEntryDTO>> byDay = teacherEntries.stream()
                .collect(Collectors.groupingBy(TimetableEntryDTO::getDayOfWeek));
        int risk = 0;
        for (List<TimetableEntryDTO> dayEntries : byDay.values()) {
            List<Integer> periods = dayEntries.stream().map(TimetableEntryDTO::getPeriodNumber).sorted().toList();
            int current = 1;
            int max = periods.isEmpty() ? 0 : 1;
            for (int index = 1; index < periods.size(); index++) {
                if (periods.get(index) == periods.get(index - 1) + 1) current++;
                else current = 1;
                max = Math.max(max, current);
            }
            risk = Math.max(risk, max);
        }
        return risk;
    }
}
