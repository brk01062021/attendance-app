package com.school.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class TeacherReplacementHistoryDTO {
    private Long scheduleId;
    private LocalDate replacementDate;
    private String className;
    private String section;
    private String subjectName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long replacedTeacherId;
    private String replacedTeacherName;
    private String status;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public LocalDate getReplacementDate() { return replacementDate; }
    public void setReplacementDate(LocalDate replacementDate) { this.replacementDate = replacementDate; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Long getReplacedTeacherId() { return replacedTeacherId; }
    public void setReplacedTeacherId(Long replacedTeacherId) { this.replacedTeacherId = replacedTeacherId; }
    public String getReplacedTeacherName() { return replacedTeacherName; }
    public void setReplacedTeacherName(String replacedTeacherName) { this.replacedTeacherName = replacedTeacherName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
