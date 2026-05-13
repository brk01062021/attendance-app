package com.school.attendance.dto;

public class TeacherReplacementTrendDTO {
    private String teacherName;
    private Long assignedReplacementCount;

    public TeacherReplacementTrendDTO(String teacherName, Long assignedReplacementCount) {
        this.teacherName = teacherName;
        this.assignedReplacementCount = assignedReplacementCount;
    }

    public String getTeacherName() { return teacherName; }
    public Long getAssignedReplacementCount() { return assignedReplacementCount; }
}