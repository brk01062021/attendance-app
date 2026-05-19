package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class PrincipalTimetableIntelligenceDTO {
    private String batchId;
    private Integer totalEntries;
    private Integer classSections;
    private Integer conflicts;
    private Integer highRiskConflicts;
    private Integer overloadRiskTeachers;
    private Integer publishReadinessScore;
    private String readinessStatus;
    private List<String> insights = new ArrayList<>();
    private List<TeacherWorkloadSummaryDTO> topWorkloadRisks = new ArrayList<>();

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public Integer getTotalEntries() { return totalEntries; }
    public void setTotalEntries(Integer totalEntries) { this.totalEntries = totalEntries; }
    public Integer getClassSections() { return classSections; }
    public void setClassSections(Integer classSections) { this.classSections = classSections; }
    public Integer getConflicts() { return conflicts; }
    public void setConflicts(Integer conflicts) { this.conflicts = conflicts; }
    public Integer getHighRiskConflicts() { return highRiskConflicts; }
    public void setHighRiskConflicts(Integer highRiskConflicts) { this.highRiskConflicts = highRiskConflicts; }
    public Integer getOverloadRiskTeachers() { return overloadRiskTeachers; }
    public void setOverloadRiskTeachers(Integer overloadRiskTeachers) { this.overloadRiskTeachers = overloadRiskTeachers; }
    public Integer getPublishReadinessScore() { return publishReadinessScore; }
    public void setPublishReadinessScore(Integer publishReadinessScore) { this.publishReadinessScore = publishReadinessScore; }
    public String getReadinessStatus() { return readinessStatus; }
    public void setReadinessStatus(String readinessStatus) { this.readinessStatus = readinessStatus; }
    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights == null ? new ArrayList<>() : insights; }
    public List<TeacherWorkloadSummaryDTO> getTopWorkloadRisks() { return topWorkloadRisks; }
    public void setTopWorkloadRisks(List<TeacherWorkloadSummaryDTO> topWorkloadRisks) { this.topWorkloadRisks = topWorkloadRisks == null ? new ArrayList<>() : topWorkloadRisks; }
}
