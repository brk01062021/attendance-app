package com.school.attendance.dto;

public class ExistingTimetableImportRowDTO {
    private String className;
    private String section;
    private String day;
    private Integer period;
    private String subject;
    private String teacher;

    public ExistingTimetableImportRowDTO() {}
    public ExistingTimetableImportRowDTO(String className, String section, String day, Integer period, String subject, String teacher) {
        this.className = className;
        this.section = section;
        this.day = day;
        this.period = period;
        this.subject = subject;
        this.teacher = teacher;
    }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public Integer getPeriod() { return period; }
    public void setPeriod(Integer period) { this.period = period; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
}
