package com.school.attendance.dto;

public class StudentDailyAttendanceDTO {

    private String date;
    private String status;
    private String subjectName;
    private String teacherName;

    public StudentDailyAttendanceDTO(String date, String status, String subjectName, String teacherName) {
        this.date = date;
        this.status = status;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
    }

    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getSubjectName() { return subjectName; }
    public String getTeacherName() { return teacherName; }
}