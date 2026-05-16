package com.school.attendance.dto;

public class TeacherWorkloadProtectionDTO {
    private Long teacherId;
    private String teacherName;
    private int scheduledPeriods;
    private int replacementPeriods;
    private int fatigueScore;
    private String riskLevel;
    private String recommendation;

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public int getScheduledPeriods() { return scheduledPeriods; }
    public void setScheduledPeriods(int scheduledPeriods) { this.scheduledPeriods = scheduledPeriods; }
    public int getReplacementPeriods() { return replacementPeriods; }
    public void setReplacementPeriods(int replacementPeriods) { this.replacementPeriods = replacementPeriods; }
    public int getFatigueScore() { return fatigueScore; }
    public void setFatigueScore(int fatigueScore) { this.fatigueScore = fatigueScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}
