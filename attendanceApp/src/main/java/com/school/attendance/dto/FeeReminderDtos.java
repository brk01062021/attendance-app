package com.school.attendance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FeeReminderDtos {
    public record Summary(Long uploadId, String schoolId, String originalFilename, String status, int totalRows, int readyRows, int invalidRows, int missingStudentRows, int missingParentMappingRows, int sentRows, int failedRows, LocalDateTime createdAt, LocalDateTime sentAt) {}
    public record Row(Long id, int rowNumber, String studentId, String studentName, String className, String section, BigDecimal pendingAmount, LocalDate dueDate, String remarks, String status, String validationMessage, String mappedParentNames) {}
    public record Preview(Summary summary, List<Row> rows) {}
    public record SendResult(Summary summary, int notificationsCreated, int rowsSent, int rowsSkipped, String message) {}
    public record History(Long id, Long uploadId, String studentId, String studentName, String className, String section, String parentName, BigDecimal pendingAmount, LocalDate dueDate, String remarks, String status, String channel, LocalDateTime sentAt) {}
}
