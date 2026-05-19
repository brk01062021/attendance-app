package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class TimetableGenerationResponseDTO {
    private String generatedBatchId;
    private Integer completionPercentage;
    private Integer totalClassesScheduled;
    private Integer totalEntries;
    private Integer conflictsDetected;
    private Integer overloadRiskTeachers;
    private List<TimetableEntryDTO> entries = new ArrayList<>();
    private List<TimetableConflictDTO> conflicts = new ArrayList<>();
    private List<TeacherWorkloadSummaryDTO> workloadSummary = new ArrayList<>();
    private List<TimetableClassSectionReviewDTO> classSectionReviews = new ArrayList<>();
    private AcademicRulesSummaryDTO academicRulesSummary;

    public String getGeneratedBatchId() { return generatedBatchId; }
    public void setGeneratedBatchId(String generatedBatchId) { this.generatedBatchId = generatedBatchId; }
    public Integer getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
    public Integer getTotalClassesScheduled() { return totalClassesScheduled; }
    public void setTotalClassesScheduled(Integer totalClassesScheduled) { this.totalClassesScheduled = totalClassesScheduled; }
    public Integer getTotalEntries() { return totalEntries; }
    public void setTotalEntries(Integer totalEntries) { this.totalEntries = totalEntries; }
    public Integer getConflictsDetected() { return conflictsDetected; }
    public void setConflictsDetected(Integer conflictsDetected) { this.conflictsDetected = conflictsDetected; }
    public Integer getOverloadRiskTeachers() { return overloadRiskTeachers; }
    public void setOverloadRiskTeachers(Integer overloadRiskTeachers) { this.overloadRiskTeachers = overloadRiskTeachers; }
    public List<TimetableEntryDTO> getEntries() { return entries; }
    public void setEntries(List<TimetableEntryDTO> entries) { this.entries = entries == null ? new ArrayList<>() : entries; }
    public List<TimetableConflictDTO> getConflicts() { return conflicts; }
    public void setConflicts(List<TimetableConflictDTO> conflicts) { this.conflicts = conflicts == null ? new ArrayList<>() : conflicts; }
    public List<TeacherWorkloadSummaryDTO> getWorkloadSummary() { return workloadSummary; }
    public void setWorkloadSummary(List<TeacherWorkloadSummaryDTO> workloadSummary) { this.workloadSummary = workloadSummary == null ? new ArrayList<>() : workloadSummary; }
    public List<TimetableClassSectionReviewDTO> getClassSectionReviews() { return classSectionReviews; }
    public void setClassSectionReviews(List<TimetableClassSectionReviewDTO> classSectionReviews) { this.classSectionReviews = classSectionReviews == null ? new ArrayList<>() : classSectionReviews; }
    public AcademicRulesSummaryDTO getAcademicRulesSummary() { return academicRulesSummary; }
    public void setAcademicRulesSummary(AcademicRulesSummaryDTO academicRulesSummary) { this.academicRulesSummary = academicRulesSummary; }
}
