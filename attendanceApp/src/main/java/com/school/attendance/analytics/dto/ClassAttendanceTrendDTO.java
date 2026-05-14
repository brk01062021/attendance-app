package com.school.attendance.analytics.dto;

public class ClassAttendanceTrendDTO {
    private String className;
    private String section;
    private Long presentCount;
    private Long absentCount;
    private Long totalCount;
    private Double attendancePercentage;

    public ClassAttendanceTrendDTO(String className, String section, Long presentCount, Long absentCount, Long totalCount, Double attendancePercentage) {
        this.className = className;
        this.section = section;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.totalCount = totalCount;
        this.attendancePercentage = attendancePercentage;
    }

    public String getClassName() { return className; }
    public String getSection() { return section; }
    public Long getPresentCount() { return presentCount; }
    public Long getAbsentCount() { return absentCount; }
    public Long getTotalCount() { return totalCount; }
    public Double getAttendancePercentage() { return attendancePercentage; }
}