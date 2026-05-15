package com.school.attendance.dto;

public class ExecutiveOverviewDTO {
    private double overallAttendancePercentage;
    private long lowAttendanceRiskStudents;
    private long classesBelowThreshold;
    private long teachersWithLeaveLoad;
    private long replacementStressTeachers;
    private long academicRiskAlerts;
    private String topPerformingClass;
    private String weakestPerformingSection;
    private double replacementStressIndex;

    public ExecutiveOverviewDTO(double overallAttendancePercentage, long lowAttendanceRiskStudents, long classesBelowThreshold, long teachersWithLeaveLoad, long replacementStressTeachers, long academicRiskAlerts, String topPerformingClass, String weakestPerformingSection, double replacementStressIndex) {
        this.overallAttendancePercentage = overallAttendancePercentage;
        this.lowAttendanceRiskStudents = lowAttendanceRiskStudents;
        this.classesBelowThreshold = classesBelowThreshold;
        this.teachersWithLeaveLoad = teachersWithLeaveLoad;
        this.replacementStressTeachers = replacementStressTeachers;
        this.academicRiskAlerts = academicRiskAlerts;
        this.topPerformingClass = topPerformingClass;
        this.weakestPerformingSection = weakestPerformingSection;
        this.replacementStressIndex = replacementStressIndex;
    }

    public double getOverallAttendancePercentage() { return overallAttendancePercentage; }
    public long getLowAttendanceRiskStudents() { return lowAttendanceRiskStudents; }
    public long getClassesBelowThreshold() { return classesBelowThreshold; }
    public long getTeachersWithLeaveLoad() { return teachersWithLeaveLoad; }
    public long getReplacementStressTeachers() { return replacementStressTeachers; }
    public long getAcademicRiskAlerts() { return academicRiskAlerts; }
    public String getTopPerformingClass() { return topPerformingClass; }
    public String getWeakestPerformingSection() { return weakestPerformingSection; }
    public double getReplacementStressIndex() { return replacementStressIndex; }
}
