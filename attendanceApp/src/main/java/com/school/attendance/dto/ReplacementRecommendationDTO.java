package com.school.attendance.dto;

public class ReplacementRecommendationDTO {
    private Long scheduleId;
    private Long teacherId;
    private String teacherName;
    private String className;
    private String section;
    private String subjectName;
    private String scheduleDate;
    private String periodTime;
    private Long replacementTeacherId;
    private String replacementTeacherName;
    private String matchType;
    private int confidenceScore;
    private int dailyWorkload;
    private boolean overloaded;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(String scheduleDate) { this.scheduleDate = scheduleDate; }
    public String getPeriodTime() { return periodTime; }
    public void setPeriodTime(String periodTime) { this.periodTime = periodTime; }
    public Long getReplacementTeacherId() { return replacementTeacherId; }
    public void setReplacementTeacherId(Long replacementTeacherId) { this.replacementTeacherId = replacementTeacherId; }
    public String getReplacementTeacherName() { return replacementTeacherName; }
    public void setReplacementTeacherName(String replacementTeacherName) { this.replacementTeacherName = replacementTeacherName; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public int getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(int confidenceScore) { this.confidenceScore = confidenceScore; }
    public int getDailyWorkload() { return dailyWorkload; }
    public void setDailyWorkload(int dailyWorkload) { this.dailyWorkload = dailyWorkload; }
    public boolean isOverloaded() { return overloaded; }
    public void setOverloaded(boolean overloaded) { this.overloaded = overloaded; }
}
