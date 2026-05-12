package com.school.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class TeacherLeaveHistoryDTO {
    private Long scheduleId;
    private LocalDate leaveDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String className;
    private String section;
    private String subjectName;
    private String leaveType;
    private String reason;
    private String status;
    private Long replacementTeacherId;
    private String replacementTeacherName;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getReplacementTeacherId() { return replacementTeacherId; }
    public void setReplacementTeacherId(Long replacementTeacherId) { this.replacementTeacherId = replacementTeacherId; }
    public String getReplacementTeacherName() { return replacementTeacherName; }
    public void setReplacementTeacherName(String replacementTeacherName) { this.replacementTeacherName = replacementTeacherName; }
}
