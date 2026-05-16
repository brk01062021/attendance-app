package com.school.attendance.dto;

public class TeacherReplacementLoadDTO {
    private Long teacherId;
    private String teacherName;
    private int scheduledPeriods;
    private int replacementPeriods;
    private int leavePeriods;
    private int overloadScore;
    private String riskLevel;

    public TeacherReplacementLoadDTO() {
    }

    public TeacherReplacementLoadDTO(Long teacherId, String teacherName, int scheduledPeriods, int replacementPeriods, int leavePeriods, int overloadScore, String riskLevel) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.scheduledPeriods = scheduledPeriods;
        this.replacementPeriods = replacementPeriods;
        this.leavePeriods = leavePeriods;
        this.overloadScore = overloadScore;
        this.riskLevel = riskLevel;
    }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public int getScheduledPeriods() { return scheduledPeriods; }
    public void setScheduledPeriods(int scheduledPeriods) { this.scheduledPeriods = scheduledPeriods; }
    public int getReplacementPeriods() { return replacementPeriods; }
    public void setReplacementPeriods(int replacementPeriods) { this.replacementPeriods = replacementPeriods; }
    public int getLeavePeriods() { return leavePeriods; }
    public void setLeavePeriods(int leavePeriods) { this.leavePeriods = leavePeriods; }
    public int getOverloadScore() { return overloadScore; }
    public void setOverloadScore(int overloadScore) { this.overloadScore = overloadScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}
