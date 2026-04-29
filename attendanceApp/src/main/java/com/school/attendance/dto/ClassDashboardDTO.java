package com.school.attendance.dto;

public class ClassDashboardDTO {

    private String className;
    private String section;
    private long totalRecords;
    private long present;
    private long absent;
    private long late;
    private double attendancePercentage;

    public ClassDashboardDTO(
            String className,
            String section,
            long totalRecords,
            long present,
            long absent,
            long late,
            double attendancePercentage
    ) {
        this.className = className;
        this.section = section;
        this.totalRecords = totalRecords;
        this.present = present;
        this.absent = absent;
        this.late = late;
        this.attendancePercentage = attendancePercentage;
    }

    public String getClassName() {
        return className;
    }

    public String getSection() {
        return section;
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