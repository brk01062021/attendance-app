package com.school.attendance.dto;

import java.time.LocalDate;

public class TeacherFatigueAlertDTO {
    private Long teacherId;
    private String teacherName;
    private LocalDate date;
    private String severity;
    private int overloadScore;
    private String reason;
    private String actionRequired;

    public TeacherFatigueAlertDTO() {
    }

    public TeacherFatigueAlertDTO(Long teacherId, String teacherName, LocalDate date, String severity, int overloadScore, String reason, String actionRequired) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.date = date;
        this.severity = severity;
        this.overloadScore = overloadScore;
        this.reason = reason;
        this.actionRequired = actionRequired;
    }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public int getOverloadScore() { return overloadScore; }
    public void setOverloadScore(int overloadScore) { this.overloadScore = overloadScore; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getActionRequired() { return actionRequired; }
    public void setActionRequired(String actionRequired) { this.actionRequired = actionRequired; }
}
