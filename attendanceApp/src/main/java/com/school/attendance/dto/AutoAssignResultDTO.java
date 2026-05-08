package com.school.attendance.dto;

public class AutoAssignResultDTO {

    private String date;
    private int totalUnassigned;
    private int assignedCount;
    private int stillUnassigned;
    private String message;

    public AutoAssignResultDTO() {
    }

    public AutoAssignResultDTO(String date, int totalUnassigned, int assignedCount, int stillUnassigned, String message) {
        this.date = date;
        this.totalUnassigned = totalUnassigned;
        this.assignedCount = assignedCount;
        this.stillUnassigned = stillUnassigned;
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTotalUnassigned() {
        return totalUnassigned;
    }

    public void setTotalUnassigned(int totalUnassigned) {
        this.totalUnassigned = totalUnassigned;
    }

    public int getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(int assignedCount) {
        this.assignedCount = assignedCount;
    }

    public int getStillUnassigned() {
        return stillUnassigned;
    }

    public void setStillUnassigned(int stillUnassigned) {
        this.stillUnassigned = stillUnassigned;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}