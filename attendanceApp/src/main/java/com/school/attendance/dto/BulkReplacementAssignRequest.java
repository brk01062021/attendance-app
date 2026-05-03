package com.school.attendance.dto;

import java.util.List;

public class BulkReplacementAssignRequest {

    private List<Long> scheduleIds;
    private Long replacementTeacherId;

    public BulkReplacementAssignRequest() {
    }

    public List<Long> getScheduleIds() {
        return scheduleIds;
    }

    public void setScheduleIds(List<Long> scheduleIds) {
        this.scheduleIds = scheduleIds;
    }

    public Long getReplacementTeacherId() {
        return replacementTeacherId;
    }

    public void setReplacementTeacherId(Long replacementTeacherId) {
        this.replacementTeacherId = replacementTeacherId;
    }
}