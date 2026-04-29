package com.school.attendance.dto;

public class TeacherWiseDashboardDTO {

    private Long teacherId;
    private String teacherName;
    private long totalRecords;
    private long present;
    private long absent;
    private long late;
    private double attendancePercentage;

    public TeacherWiseDashboardDTO(
            Long teacherId,
            String teacherName,
            long totalRecords,
            long present,
            long absent,
            long late,
            double attendancePercentage
    ) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.totalRecords = totalRecords;
        this.present = present;
        this.absent = absent;
        this.late = late;
        this.attendancePercentage = attendancePercentage;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public long getPresent() {
        return present;
    }

    public long getAbsent() {
        return absent;
    }

    public long getLate() {
        return late;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }
}