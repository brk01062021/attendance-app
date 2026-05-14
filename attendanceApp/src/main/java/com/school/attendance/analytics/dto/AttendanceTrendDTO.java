package com.school.attendance.analytics.dto;

public class AttendanceTrendDTO {
    private String date;
    private Long presentCount;
    private Long absentCount;
    private Long totalCount;
    private Double attendancePercentage;

    public AttendanceTrendDTO(String date, Long presentCount, Long absentCount, Long totalCount, Double attendancePercentage) {
        this.date = date;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.totalCount = totalCount;
        this.attendancePercentage = attendancePercentage;
    }

    public String getDate() { return date; }
    public Long getPresentCount() { return presentCount; }
    public Long getAbsentCount() { return absentCount; }
    public Long getTotalCount() { return totalCount; }
    public Double getAttendancePercentage() { return attendancePercentage; }
}