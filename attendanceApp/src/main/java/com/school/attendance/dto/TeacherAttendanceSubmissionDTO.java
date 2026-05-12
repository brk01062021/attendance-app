package com.school.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class TeacherAttendanceSubmissionDTO {
    private Long scheduleId;
    private LocalDate attendanceDate;
    private String className;
    private String section;
    private String subjectName;
    private LocalTime submittedTime;
    private Integer totalStudents;
    private Integer presentStudents;
    private Integer absentStudents;
    private String status;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public LocalTime getSubmittedTime() { return submittedTime; }
    public void setSubmittedTime(LocalTime submittedTime) { this.submittedTime = submittedTime; }
    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
    public Integer getPresentStudents() { return presentStudents; }
    public void setPresentStudents(Integer presentStudents) { this.presentStudents = presentStudents; }
    public Integer getAbsentStudents() { return absentStudents; }
    public void setAbsentStudents(Integer absentStudents) { this.absentStudents = absentStudents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
