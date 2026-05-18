package com.school.attendance.dto;

public class TeacherWorkloadSummaryDTO {
    private Long teacherId;
    private String teacherName;
    private Integer weeklyPeriods;
    private Integer replacementLoad;
    private Integer continuousPeriodRisk;
    private Integer freeGapCount;
    private Integer overloadRiskScore;
    private String status;

    public TeacherWorkloadSummaryDTO() {
    }

    public TeacherWorkloadSummaryDTO(Long teacherId, String teacherName, Integer weeklyPeriods, Integer replacementLoad,
                                     Integer continuousPeriodRisk, Integer freeGapCount, Integer overloadRiskScore, String status) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.weeklyPeriods = weeklyPeriods;
        this.replacementLoad = replacementLoad;
        this.continuousPeriodRisk = continuousPeriodRisk;
        this.freeGapCount = freeGapCount;
        this.overloadRiskScore = overloadRiskScore;
        this.status = status;
    }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public Integer getWeeklyPeriods() { return weeklyPeriods; }
    public void setWeeklyPeriods(Integer weeklyPeriods) { this.weeklyPeriods = weeklyPeriods; }
    public Integer getReplacementLoad() { return replacementLoad; }
    public void setReplacementLoad(Integer replacementLoad) { this.replacementLoad = replacementLoad; }
    public Integer getContinuousPeriodRisk() { return continuousPeriodRisk; }
    public void setContinuousPeriodRisk(Integer continuousPeriodRisk) { this.continuousPeriodRisk = continuousPeriodRisk; }
    public Integer getFreeGapCount() { return freeGapCount; }
    public void setFreeGapCount(Integer freeGapCount) { this.freeGapCount = freeGapCount; }
    public Integer getOverloadRiskScore() { return overloadRiskScore; }
    public void setOverloadRiskScore(Integer overloadRiskScore) { this.overloadRiskScore = overloadRiskScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
