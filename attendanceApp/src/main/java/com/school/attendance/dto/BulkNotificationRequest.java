package com.school.attendance.dto;

import java.util.List;

public class BulkNotificationRequest {
    private Long schoolId;
    private String role; // PARENT, STUDENT, TEACHER, ADMIN
    private String className;
    private String section;
    private List<Long> userIds;

    private String title;
    private String message;
    private String type; // WEEKLY_ATTENDANCE, MONTHLY_ATTENDANCE, SCHOOL_ALERT, EXAM_RESULT

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}