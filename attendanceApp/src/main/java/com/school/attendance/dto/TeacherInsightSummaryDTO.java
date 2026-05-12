package com.school.attendance.dto;

import java.util.List;

public class TeacherInsightSummaryDTO {

    private Long teacherId;
    private String teacherName;
    private List<String> classesHandled;
    private List<String> sectionsHandled;
    private List<String> subjectsHandled;
    private Integer totalLeaves;
    private Integer plannedLeaves;
    private Integer unplannedLeaves;
    private Integer replacementAssignments;
    private Integer attendanceSubmissions;
    private Integer examResultSubmissions;

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public List<String> getClassesHandled() { return classesHandled; }
    public void setClassesHandled(List<String> classesHandled) { this.classesHandled = classesHandled; }
    public List<String> getSectionsHandled() { return sectionsHandled; }
    public void setSectionsHandled(List<String> sectionsHandled) { this.sectionsHandled = sectionsHandled; }
    public List<String> getSubjectsHandled() { return subjectsHandled; }
    public void setSubjectsHandled(List<String> subjectsHandled) { this.subjectsHandled = subjectsHandled; }
    public Integer getTotalLeaves() { return totalLeaves; }
    public void setTotalLeaves(Integer totalLeaves) { this.totalLeaves = totalLeaves; }
    public Integer getPlannedLeaves() { return plannedLeaves; }
    public void setPlannedLeaves(Integer plannedLeaves) { this.plannedLeaves = plannedLeaves; }
    public Integer getUnplannedLeaves() { return unplannedLeaves; }
    public void setUnplannedLeaves(Integer unplannedLeaves) { this.unplannedLeaves = unplannedLeaves; }
    public Integer getReplacementAssignments() { return replacementAssignments; }
    public void setReplacementAssignments(Integer replacementAssignments) { this.replacementAssignments = replacementAssignments; }
    public Integer getAttendanceSubmissions() { return attendanceSubmissions; }
    public void setAttendanceSubmissions(Integer attendanceSubmissions) { this.attendanceSubmissions = attendanceSubmissions; }
    public Integer getExamResultSubmissions() { return examResultSubmissions; }
    public void setExamResultSubmissions(Integer examResultSubmissions) { this.examResultSubmissions = examResultSubmissions; }
}
