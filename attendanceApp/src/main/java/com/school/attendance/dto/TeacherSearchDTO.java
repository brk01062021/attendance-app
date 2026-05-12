package com.school.attendance.dto;

public class TeacherSearchDTO {

    private Long teacherId;
    private String teacherName;

    public TeacherSearchDTO() {
    }

    public TeacherSearchDTO(Long teacherId, String teacherName) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
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
}
