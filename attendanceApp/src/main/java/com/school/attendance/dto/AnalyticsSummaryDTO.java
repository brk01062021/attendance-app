package com.school.attendance.dto;

public class AnalyticsSummaryDTO {
    private Long totalStudents;
    private Long totalPresent;
    private Long totalAbsent;
    private Double attendancePercentage;
    private Long totalTeacherLeaves;
    private Long totalReplacementsAssigned;
    private Long totalReplacementsMissing;

    public AnalyticsSummaryDTO(
            Long totalStudents,
            Long totalPresent,
            Long totalAbsent,
            Double attendancePercentage,
            Long totalTeacherLeaves,
            Long totalReplacementsAssigned,
            Long totalReplacementsMissing
    ) {
        this.totalStudents = totalStudents;
        this.totalPresent = totalPresent;
        this.totalAbsent = totalAbsent;
        this.attendancePercentage = attendancePercentage;
        this.totalTeacherLeaves = totalTeacherLeaves;
        this.totalReplacementsAssigned = totalReplacementsAssigned;
        this.totalReplacementsMissing = totalReplacementsMissing;
    }

    public Long getTotalStudents() { return totalStudents; }
    public Long getTotalPresent() { return totalPresent; }
    public Long getTotalAbsent() { return totalAbsent; }
    public Double getAttendancePercentage() { return attendancePercentage; }
    public Long getTotalTeacherLeaves() { return totalTeacherLeaves; }
    public Long getTotalReplacementsAssigned() { return totalReplacementsAssigned; }
    public Long getTotalReplacementsMissing() { return totalReplacementsMissing; }
}