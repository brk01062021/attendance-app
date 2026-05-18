package com.school.attendance.dto;

public class TimetableConflictDTO {
    private String id;
    private String severity;
    private String type;
    private String title;
    private String description;
    private String className;
    private String section;
    private String teacherName;
    private String dayOfWeek;
    private Integer periodNumber;

    public TimetableConflictDTO() {
    }

    public TimetableConflictDTO(String id, String severity, String type, String title, String description,
                                String className, String section, String teacherName, String dayOfWeek, Integer periodNumber) {
        this.id = id;
        this.severity = severity;
        this.type = type;
        this.title = title;
        this.description = description;
        this.className = className;
        this.section = section;
        this.teacherName = teacherName;
        this.dayOfWeek = dayOfWeek;
        this.periodNumber = periodNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }
}
