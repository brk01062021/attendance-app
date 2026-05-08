package com.school.attendance.dto;

import lombok.Data;

@Data
public class NotificationRequest {

    private Long userId;
    private String role;
    private String title;
    private String message;
    private String type;
}