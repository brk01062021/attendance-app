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
}