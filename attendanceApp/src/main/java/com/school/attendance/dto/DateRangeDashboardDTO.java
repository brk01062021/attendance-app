package com.school.attendance.dto;

public class DateRangeDashboardDTO {

    private String date;
    private long totalRecords;
    private long present;
    private long absent;
    private long late;
    private double attendancePercentage;

    public DateRangeDashboardDTO(
            String date,
            long totalRecords,
            long present,
            long absent,
            long late,
            double attendancePercentage
    ) {
        this.date = date;
        this.totalRecords = totalRecords;
        this.present = present;
        this.absent = absent;
        this.late = late;
        this.attendancePercentage = attendancePercentage;
    }

    public String getDate() {
        return date;
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