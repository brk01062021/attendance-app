package com.school.attendance.dto;

public class PrincipalDashboardSummaryDTO {
    private long totalStudents;
    private long totalTeachers;
    private double todayAttendancePercentage;
    private long studentsAbsentToday;
    private long teachersOnLeave;
    private long replacementPeriodsToday;
    private long lowAttendanceStudents;
    private long pendingTeacherAttendance;

    public PrincipalDashboardSummaryDTO(long totalStudents, long totalTeachers, double todayAttendancePercentage, long studentsAbsentToday, long teachersOnLeave, long replacementPeriodsToday, long lowAttendanceStudents, long pendingTeacherAttendance) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.todayAttendancePercentage = todayAttendancePercentage;
        this.studentsAbsentToday = studentsAbsentToday;
        this.teachersOnLeave = teachersOnLeave;
        this.replacementPeriodsToday = replacementPeriodsToday;
        this.lowAttendanceStudents = lowAttendanceStudents;
        this.pendingTeacherAttendance = pendingTeacherAttendance;
    }

    public long getTotalStudents() { return totalStudents; }
    public long getTotalTeachers() { return totalTeachers; }
    public double getTodayAttendancePercentage() { return todayAttendancePercentage; }
    public long getStudentsAbsentToday() { return studentsAbsentToday; }
    public long getTeachersOnLeave() { return teachersOnLeave; }
    public long getReplacementPeriodsToday() { return replacementPeriodsToday; }
    public long getLowAttendanceStudents() { return lowAttendanceStudents; }
    public long getPendingTeacherAttendance() { return pendingTeacherAttendance; }
}
