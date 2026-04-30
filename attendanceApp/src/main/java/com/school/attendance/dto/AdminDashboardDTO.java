package com.school.attendance.dto;

public class AdminDashboardDTO {

    private String attendanceDate;
    private long totalStudents;
    private long presentStudents;
    private long absentStudents;
    private long lateStudents;
    private double attendancePercentage;

    public AdminDashboardDTO(
            String attendanceDate,
            long totalStudents,
            long presentStudents,
            long absentStudents,
            long lateStudents,
            double attendancePercentage
    ) {
        this.attendanceDate = attendanceDate;
        this.totalStudents = totalStudents;
        this.presentStudents = presentStudents;
        this.absentStudents = absentStudents;
        this.lateStudents = lateStudents;
        this.attendancePercentage = attendancePercentage;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public long getPresentStudents() {
        return presentStudents;
    }

    public long getAbsentStudents() {
        return absentStudents;
    }

    public long getLateStudents() {
        return lateStudents;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }
}