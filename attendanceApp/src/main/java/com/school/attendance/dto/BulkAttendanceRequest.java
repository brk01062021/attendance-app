package com.school.attendance.dto;

import java.util.List;

public class BulkAttendanceRequest {

    private List<AttendanceRequest> attendanceList;

    public List<AttendanceRequest> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<AttendanceRequest> attendanceList) {
        this.attendanceList = attendanceList;
    }
}