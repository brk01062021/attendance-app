package com.school.attendance.dto;

public class ClassComparisonDTO {
    private String className;
    private String section;
    private long presentCount;
    private long absentCount;
    private long totalMarked;
    private double attendancePercentage;

    public ClassComparisonDTO(String className, String section, long presentCount, long absentCount, long totalMarked, double attendancePercentage) {
        this.className = className;
        this.section = section;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.totalMarked = totalMarked;
        this.attendancePercentage = attendancePercentage;
    }

    public String getClassName() { return className; }
    public String getSection() { return section; }
    public long getPresentCount() { return presentCount; }
    public long getAbsentCount() { return absentCount; }
    public long getTotalMarked() { return totalMarked; }
    public double getAttendancePercentage() { return attendancePercentage; }
}
