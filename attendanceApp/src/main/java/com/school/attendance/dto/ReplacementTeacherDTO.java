package com.school.attendance.dto;

public class ReplacementTeacherDTO {

    private Long teacherId;
    private String teacherName;
    private String className;
    private String section;
    private String subjectName;
    private String matchType;

    private int dailyWorkload;
    private String nextClass;
    private String lastClassEnded;

    private Integer gapScore;
    private Integer overloadScore;
    private Boolean fatigueRisk;
    private Integer consecutivePeriods;
    private Boolean preferredSubjectMatch;
    private Integer replacementPriorityScore;

    public ReplacementTeacherDTO() {
    }

    public ReplacementTeacherDTO(
            Long teacherId,
            String teacherName,
            String className,
            String section,
            String subjectName,
            String matchType
    ) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.className = className;
        this.section = section;
        this.subjectName = subjectName;
        this.matchType = matchType;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getClassName() {
        return className;
    }

    public String getSection() {
        return section;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public int getDailyWorkload() {
        return dailyWorkload;
    }

    public void setDailyWorkload(int dailyWorkload) {
        this.dailyWorkload = dailyWorkload;
    }

    public String getNextClass() {
        return nextClass;
    }

    public void setNextClass(String nextClass) {
        this.nextClass = nextClass;
    }

    public String getLastClassEnded() {
        return lastClassEnded;
    }

    public void setLastClassEnded(String lastClassEnded) {
        this.lastClassEnded = lastClassEnded;
    }

    public Integer getGapScore() {
        return gapScore;
    }

    public void setGapScore(Integer gapScore) {
        this.gapScore = gapScore;
    }

    public Integer getOverloadScore() { return overloadScore; }
    public void setOverloadScore(Integer overloadScore) { this.overloadScore = overloadScore; }
    public Boolean getFatigueRisk() { return fatigueRisk; }
    public void setFatigueRisk(Boolean fatigueRisk) { this.fatigueRisk = fatigueRisk; }
    public Integer getConsecutivePeriods() { return consecutivePeriods; }
    public void setConsecutivePeriods(Integer consecutivePeriods) { this.consecutivePeriods = consecutivePeriods; }
    public Boolean getPreferredSubjectMatch() { return preferredSubjectMatch; }
    public void setPreferredSubjectMatch(Boolean preferredSubjectMatch) { this.preferredSubjectMatch = preferredSubjectMatch; }
    public Integer getReplacementPriorityScore() { return replacementPriorityScore; }
    public void setReplacementPriorityScore(Integer replacementPriorityScore) { this.replacementPriorityScore = replacementPriorityScore; }
}