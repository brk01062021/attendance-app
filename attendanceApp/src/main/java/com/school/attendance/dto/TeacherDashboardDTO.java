package com.school.attendance.dto;

public class TeacherDashboardDTO {

    private Long teacherId;
    private String teacherName;
    private long totalStudents;
    private long present;
    private long absent;
    private long late;
    private double attendancePercentage;

    public TeacherDashboardDTO(
            Long teacherId,
            String teacherName,
            long totalStudents,
            long present,
            long absent,
            long late,
            double attendancePercentage
    ) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.totalStudents = totalStudents;
        this.present = present;
        this.absent = absent;
        this.late = late;
        this.attendancePercentage = attendancePercentage;
    }

    public Long getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public long getTotalStudents() { return totalStudents; }
    public long getPresent() { return present; }
    public long getAbsent() { return absent; }
    public long getLate() { return late; }
    public double getAttendancePercentage() { return attendancePercentage; }
}