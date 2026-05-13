package com.school.attendance.dto;

public class SectionAnalyticsDTO {

    private String className;
    private String section;
    private long totalStudents;
    private double attendancePercentage;
    private double passPercentage;
    private long absentees;

    public SectionAnalyticsDTO() {
    }

    public SectionAnalyticsDTO(
            String className,
            String section,
            long totalStudents,
            double attendancePercentage,
            double passPercentage,
            long absentees
    ) {
        this.className = className;
        this.section = section;
        this.totalStudents = totalStudents;
        this.attendancePercentage = attendancePercentage;
        this.passPercentage = passPercentage;
        this.absentees = absentees;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public long getAbsentees() {
        return absentees;
    }

    public void setAbsentees(long absentees) {
        this.absentees = absentees;
    }
}
