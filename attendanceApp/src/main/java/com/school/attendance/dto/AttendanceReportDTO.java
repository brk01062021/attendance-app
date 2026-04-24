package com.school.attendance.dto;

public class AttendanceReportDTO {

    private Long studentId;
    private String studentName;
    private String className;
    private String section;
    private long totalDays;
    private long presentDays;
    private long absentDays;
    private double attendancePercentage;

    public AttendanceReportDTO(Long studentId, String studentName, String className,
                               String section, long totalDays, long presentDays,
                               long absentDays) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.section = section;
        this.totalDays = totalDays;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.attendancePercentage = totalDays == 0 ? 0 : (presentDays * 100.0) / totalDays;
    }

    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getClassName() { return className; }
    public String getSection() { return section; }
    public long getTotalDays() { return totalDays; }
    public long getPresentDays() { return presentDays; }
    public long getAbsentDays() { return absentDays; }
    public double getAttendancePercentage() { return attendancePercentage; }
}