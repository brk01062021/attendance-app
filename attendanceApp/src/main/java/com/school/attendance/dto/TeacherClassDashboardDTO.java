package com.school.attendance.dto;

public class TeacherClassDashboardDTO {

    private String className;
    private String section;
    private String subjectName;
    private long totalStudents;
    private long present;
    private long absent;
    private long late;
    private double attendancePercentage;

    public TeacherClassDashboardDTO(
            String className,
            String section,
            String subjectName,
            long totalStudents,
            long present,
            long absent,
            long late,
            double attendancePercentage
    ) {
        this.className = className;
        this.section = section;
        this.subjectName = subjectName;
        this.totalStudents = totalStudents;
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

    public String getSubjectName() {
        return subjectName;
    }

    public long getTotalStudents() {
        return totalStudents;
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