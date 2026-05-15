package com.school.attendance.dto;

public class TeacherWorkloadDTO {
    private Long teacherId;
    private String teacherName;
    private long scheduledPeriods;
    private long replacementPeriods;
    private long plannedLeaves;
    private long unplannedLeaves;
    private double workloadScore;
    private String riskLevel;

    public TeacherWorkloadDTO(Long teacherId, String teacherName, long scheduledPeriods, long replacementPeriods, long plannedLeaves, long unplannedLeaves, double workloadScore, String riskLevel) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.scheduledPeriods = scheduledPeriods;
        this.replacementPeriods = replacementPeriods;
        this.plannedLeaves = plannedLeaves;
        this.unplannedLeaves = unplannedLeaves;
        this.workloadScore = workloadScore;
        this.riskLevel = riskLevel;
    }

    public Long getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public long getScheduledPeriods() { return scheduledPeriods; }
    public long getReplacementPeriods() { return replacementPeriods; }
    public long getPlannedLeaves() { return plannedLeaves; }
    public long getUnplannedLeaves() { return unplannedLeaves; }
    public double getWorkloadScore() { return workloadScore; }
    public String getRiskLevel() { return riskLevel; }
}
