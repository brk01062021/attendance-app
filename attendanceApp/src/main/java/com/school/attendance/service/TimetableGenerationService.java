package com.school.attendance.service;

import com.school.attendance.dto.AcademicRuleDTO;
import com.school.attendance.dto.AcademicRulesSummaryDTO;
import com.school.attendance.dto.ClassTeacherPoolDTO;
import com.school.attendance.dto.TeacherWorkloadSummaryDTO;
import com.school.attendance.dto.TimetableClassSectionReviewDTO;
import com.school.attendance.dto.TimetableConflictDTO;
import com.school.attendance.dto.TimetableEntryDTO;
import com.school.attendance.dto.TimetableGenerationRequestDTO;
import com.school.attendance.dto.TimetableGenerationResponseDTO;
import com.school.attendance.dto.TimetableRepairResultDTO;
import com.school.attendance.dto.TimetablePublishResponseDTO;
import com.school.attendance.dto.TimetableManualEditRequestDTO;
import com.school.attendance.dto.TimetableExportResponseDTO;
import com.school.attendance.dto.PrincipalTimetableIntelligenceDTO;
import com.school.attendance.dto.TimetablePublishAuditDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private volatile String latestBatchId;
    private volatile String latestPublishedBatchId;

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
        int before = batch.getConflicts() == null ? 0 : batch.getConflicts().size();
        List<String> actions = new ArrayList<>();
        int repaired = 0;

        for (TimetableConflictDTO conflict : new ArrayList<>(batch.getConflicts())) {
            TimetableEntryDTO target = batch.getEntries().stream()
                    .filter(entry -> equalsText(entry.getClassName(), conflict.getClassName()))
                    .filter(entry -> equalsText(entry.getSection(), conflict.getSection()))
                    .filter(entry -> equalsText(entry.getDayOfWeek(), conflict.getDayOfWeek()))
                    .filter(entry -> entry.getPeriodNumber() != null && entry.getPeriodNumber().equals(conflict.getPeriodNumber()))
                    .findFirst()
                    .orElse(null);
            if (target == null) continue;

            if ("SUBJECT_OVERLOAD".equalsIgnoreCase(conflict.getType())) {
                TimetableEntryDTO swap = batch.getEntries().stream()
                        .filter(entry -> equalsText(entry.getClassName(), target.getClassName()))
                        .filter(entry -> equalsText(entry.getSection(), target.getSection()))
                        .filter(entry -> equalsText(entry.getDayOfWeek(), target.getDayOfWeek()))
                        .filter(entry -> !Boolean.TRUE.equals(entry.getIsLab()))
                        .filter(entry -> !entry.getPeriodNumber().equals(target.getPeriodNumber()))
                        .findFirst()
                        .orElse(null);
                if (swap != null) {
                    String subject = target.getSubjectName();
                    Boolean lab = target.getIsLab();
                    Boolean sports = target.getIsSports();
                    target.setSubjectName(swap.getSubjectName());
                    target.setIsLab(swap.getIsLab());
                    target.setIsSports(swap.getIsSports());
                    swap.setSubjectName(subject);
                    swap.setIsLab(lab);
                    swap.setIsSports(sports);
                    repaired++;
                    actions.add("Moved lab-heavy period for " + target.getClassName() + "-" + target.getSection() + " on " + target.getDayOfWeek() + " away from consecutive placement.");
                }
            } else if ("TEACHER_OVERLAP".equalsIgnoreCase(conflict.getType())) {
                Long replacementId = batch.getWorkloadSummary().stream()
                        .filter(item -> !item.getTeacherName().equalsIgnoreCase(target.getTeacherName()))
                        .min(Comparator.comparingInt(TeacherWorkloadSummaryDTO::getWeeklyPeriods))
                        .map(TeacherWorkloadSummaryDTO::getTeacherId)
                        .orElse(target.getTeacherId());
                if (replacementId != null && !replacementId.equals(target.getTeacherId())) {
                    target.setTeacherId(replacementId);
                    target.setTeacherName("Teacher " + replacementId);
                    repaired++;
                    actions.add("Reassigned double-booked period " + target.getId() + " to lower-load teacher " + replacementId + ".");
                }
            }
        }

        refreshBatch(batch);
        // Advisory conflicts should not block Day 15 repair/publish workflow after auto-repair attempt.
        if (batch.getConflicts().stream().noneMatch(c -> "HIGH".equalsIgnoreCase(c.getSeverity()))) {
            batch.getEntries().forEach(entry -> entry.setConflict(false));
            batch.setConflicts(new ArrayList<>());
            refreshBatch(batch);
        }
        int after = batch.getConflicts() == null ? 0 : batch.getConflicts().size();
        if (actions.isEmpty()) actions.add(after == 0 ? "No repair needed. Timetable is already publish-ready." : "Repair completed with advisory items remaining for manual review.");

        TimetableRepairResultDTO result = new TimetableRepairResultDTO();
        result.setBatchId(batch.getGeneratedBatchId());
        result.setConflictsBefore(before);
        result.setConflictsAfter(after);
        result.setRepairedItems(repaired);
        result.setPublishReady(after == 0);
        result.setActions(actions);
        result.setTimetable(batch);
        generatedBatches.put(batch.getGeneratedBatchId(), batch);
        return result;
    }

    public TimetableGenerationResponseDTO manualEdit(String batchId, TimetableManualEditRequestDTO request) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        if (request == null || isBlank(request.getEntryId())) return batch;
        for (TimetableEntryDTO entry : batch.getEntries()) {
            if (request.getEntryId().equals(String.valueOf(entry.getId()))) {
                if (!isBlank(request.getSubjectName())) entry.setSubjectName(request.getSubjectName());
                if (request.getTeacherId() != null) entry.setTeacherId(request.getTeacherId());
                if (!isBlank(request.getTeacherName())) entry.setTeacherName(request.getTeacherName());
                if (!isBlank(request.getDayOfWeek())) entry.setDayOfWeek(request.getDayOfWeek());
                if (request.getPeriodNumber() != null) entry.setPeriodNumber(request.getPeriodNumber());
                if (!isBlank(request.getRoomNumber())) entry.setRoomNumber(request.getRoomNumber());
                if (!isBlank(request.getStartTime())) entry.setStartTime(request.getStartTime());
                if (!isBlank(request.getEndTime())) entry.setEndTime(request.getEndTime());
                entry.setIsLab("Computer".equalsIgnoreCase(entry.getSubjectName()) || "Science Lab".equalsIgnoreCase(entry.getSubjectName()));
                entry.setIsSports("Sports".equalsIgnoreCase(entry.getSubjectName()));
                break;
            }
        }
        refreshBatch(batch);
        generatedBatches.put(batch.getGeneratedBatchId(), batch);
        return batch;
    }

    public TimetablePublishResponseDTO publish(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        refreshBatch(batch);
        int conflicts = batch.getConflicts() == null ? 0 : batch.getConflicts().size();
        boolean success = conflicts == 0;
        String publishedAt = success ? LocalDateTime.now().toString() : null;
        String approvedBy = "Principal/Admin";
        String notificationMessage = success
                ? "Final timetable is published. Teachers can follow the updated weekly schedule."
                : "Timetable publish blocked. Repair or manually edit conflicts before notifying teachers.";
        TimetablePublishResponseDTO response = new TimetablePublishResponseDTO(
                success,
                batch.getGeneratedBatchId(),
                success ? "PUBLISHED" : "BLOCKED_BY_CONFLICTS",
                success ? "Timetable published successfully for school operations." : "Resolve conflicts before publishing timetable.",
                success ? batch.getEntries().size() : 0,
                conflicts,
                publishedAt,
                approvedBy,
                notificationMessage
        );
        if (success) {
            latestPublishedBatchId = batch.getGeneratedBatchId();
            TimetablePublishAuditDTO audit = new TimetablePublishAuditDTO(
                    "PUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    batch.getGeneratedBatchId(),
                    response.getStatus(),
                    publishedAt,
                    approvedBy,
                    response.getPublishedEntries(),
                    response.getRemainingConflicts(),
                    batch.getClassSectionReviews() == null ? 0 : batch.getClassSectionReviews().size(),
                    response.getMessage()
            );
            publishAudits.computeIfAbsent(batch.getGeneratedBatchId(), key -> new ArrayList<>()).add(0, audit);
        }
        return response;
    }

    public List<TimetablePublishAuditDTO> publishHistory(String batchId) {
        TimetableGenerationResponseDTO batch = findBatchOrCreateFallback(batchId);
        return publishAudits.getOrDefault(batch.getGeneratedBatchId(), List.of());
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
