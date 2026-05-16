package com.school.attendance.dto;

import java.time.LocalDate;

public class TeacherWorkloadInsightDTO {
    private Long teacherId;
    private String teacherName;
    private LocalDate date;
    private int scheduledPeriods;
    private int replacementPeriods;
    private int totalPeriods;
    private int consecutivePeriods;
    private int freePeriodGaps;
    private int overloadScore;
    private String riskLevel;
    private String recommendation;

    public TeacherWorkloadInsightDTO() {
    }

    public TeacherWorkloadInsightDTO(Long teacherId, String teacherName, LocalDate date, int scheduledPeriods, int replacementPeriods, int totalPeriods, int consecutivePeriods, int freePeriodGaps, int overloadScore, String riskLevel, String recommendation) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.date = date;
        this.scheduledPeriods = scheduledPeriods;
        this.replacementPeriods = replacementPeriods;
        this.totalPeriods = totalPeriods;
        this.consecutivePeriods = consecutivePeriods;
        this.freePeriodGaps = freePeriodGaps;
        this.overloadScore = overloadScore;
        this.riskLevel = riskLevel;
        this.recommendation = recommendation;
    }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getScheduledPeriods() { return scheduledPeriods; }
    public void setScheduledPeriods(int scheduledPeriods) { this.scheduledPeriods = scheduledPeriods; }
    public int getReplacementPeriods() { return replacementPeriods; }
    public void setReplacementPeriods(int replacementPeriods) { this.replacementPeriods = replacementPeriods; }
    public int getTotalPeriods() { return totalPeriods; }
    public void setTotalPeriods(int totalPeriods) { this.totalPeriods = totalPeriods; }
    public int getConsecutivePeriods() { return consecutivePeriods; }
    public void setConsecutivePeriods(int consecutivePeriods) { this.consecutivePeriods = consecutivePeriods; }
    public int getFreePeriodGaps() { return freePeriodGaps; }
    public void setFreePeriodGaps(int freePeriodGaps) { this.freePeriodGaps = freePeriodGaps; }
    public int getOverloadScore() { return overloadScore; }
    public void setOverloadScore(int overloadScore) { this.overloadScore = overloadScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}
