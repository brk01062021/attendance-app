package com.school.attendance.dto;

public class SubjectDashboardDTO {

    private String subjectName;
    private long totalRecords;
    private long present;
    private long absent;
    private long late;
    private double attendancePercentage;

    public SubjectDashboardDTO(
            String subjectName,
            long totalRecords,
            long present,
            long absent,
            long late,
            double attendancePercentage
    ) {
        this.subjectName = subjectName;
        this.totalRecords = totalRecords;
        this.present = present;
        this.absent = absent;
        this.late = late;
        this.attendancePercentage = attendancePercentage;
    }

    public String getSubjectName() {
        return subjectName;
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