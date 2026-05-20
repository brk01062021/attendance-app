package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class TimetableRolloutReadinessDTO {
    private String batchId;
    private Boolean readyForRollout;
    private Boolean locked;
    private Boolean latestPublished;
    private Integer totalEntries;
    private Integer teacherVisibleEntries;
    private Integer studentParentVisibleEntries;
    private Integer conflicts;
    private Integer notifications;
    private Integer versions;
    private Integer readinessScore;
    private List<String> blockers = new ArrayList<>();
    private List<String> checks = new ArrayList<>();

    public TimetableRolloutReadinessDTO() {}

    public TimetableRolloutReadinessDTO(String batchId, Boolean readyForRollout, Boolean locked, Boolean latestPublished, Integer totalEntries, Integer teacherVisibleEntries, Integer studentParentVisibleEntries, Integer conflicts, Integer notifications, Integer versions, Integer readinessScore, List<String> blockers, List<String> checks) {
        this.batchId = batchId;
        this.readyForRollout = readyForRollout;
        this.locked = locked;
        this.latestPublished = latestPublished;
        this.totalEntries = totalEntries;
        this.teacherVisibleEntries = teacherVisibleEntries;
        this.studentParentVisibleEntries = studentParentVisibleEntries;
        this.conflicts = conflicts;
        this.notifications = notifications;
        this.versions = versions;
        this.readinessScore = readinessScore;
        this.blockers = blockers == null ? new ArrayList<>() : blockers;
        this.checks = checks == null ? new ArrayList<>() : checks;
    }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public Boolean getReadyForRollout() { return readyForRollout; }
    public void setReadyForRollout(Boolean readyForRollout) { this.readyForRollout = readyForRollout; }
    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }
    public Boolean getLatestPublished() { return latestPublished; }
    public void setLatestPublished(Boolean latestPublished) { this.latestPublished = latestPublished; }
    public Integer getTotalEntries() { return totalEntries; }
    public void setTotalEntries(Integer totalEntries) { this.totalEntries = totalEntries; }
    public Integer getTeacherVisibleEntries() { return teacherVisibleEntries; }
    public void setTeacherVisibleEntries(Integer teacherVisibleEntries) { this.teacherVisibleEntries = teacherVisibleEntries; }
    public Integer getStudentParentVisibleEntries() { return studentParentVisibleEntries; }
    public void setStudentParentVisibleEntries(Integer studentParentVisibleEntries) { this.studentParentVisibleEntries = studentParentVisibleEntries; }
    public Integer getConflicts() { return conflicts; }
    public void setConflicts(Integer conflicts) { this.conflicts = conflicts; }
    public Integer getNotifications() { return notifications; }
    public void setNotifications(Integer notifications) { this.notifications = notifications; }
    public Integer getVersions() { return versions; }
    public void setVersions(Integer versions) { this.versions = versions; }
    public Integer getReadinessScore() { return readinessScore; }
    public void setReadinessScore(Integer readinessScore) { this.readinessScore = readinessScore; }
    public List<String> getBlockers() { return blockers; }
    public void setBlockers(List<String> blockers) { this.blockers = blockers == null ? new ArrayList<>() : blockers; }
    public List<String> getChecks() { return checks; }
    public void setChecks(List<String> checks) { this.checks = checks == null ? new ArrayList<>() : checks; }
}
