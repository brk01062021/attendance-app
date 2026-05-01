package com.school.attendance.dto;

import java.time.LocalDate;

public class AdminStudentAttendanceDTO {

    private Long studentId;
    private String studentName;
    private String className;
    private String section;
    private String subjectName;
    private String status;
    private LocalDate attendanceDate;

    public AdminStudentAttendanceDTO(
            Long studentId,
            String studentName,
            String className,
            String section,
            String subjectName,
            String status,
            LocalDate attendanceDate
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.section = section;
        this.subjectName = subjectName;
        this.status = status;
        this.attendanceDate = attendanceDate;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
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

    public String getStatus() {
        return status;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }
}