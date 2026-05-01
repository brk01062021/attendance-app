package com.school.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class TeacherSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long teacherId;
    private String teacherName;

    private String className;
    private String section;
    private String subjectName;

    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private TeacherScheduleStatus status = TeacherScheduleStatus.AVAILABLE;

    private Long replacementTeacherId;
    private String replacementTeacherName;

    private Boolean replacementClass = false;

    public Long getId() {
        return id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public TeacherScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(TeacherScheduleStatus status) {
        this.status = status;
    }

    public Long getReplacementTeacherId() {
        return replacementTeacherId;
    }

    public void setReplacementTeacherId(Long replacementTeacherId) {
        this.replacementTeacherId = replacementTeacherId;
    }

    public String getReplacementTeacherName() {
        return replacementTeacherName;
    }

    public void setReplacementTeacherName(String replacementTeacherName) {
        this.replacementTeacherName = replacementTeacherName;
    }

    public Boolean getReplacementClass() {
        return replacementClass;
    }

    public void setReplacementClass(Boolean replacementClass) {
        this.replacementClass = replacementClass;
    }
}