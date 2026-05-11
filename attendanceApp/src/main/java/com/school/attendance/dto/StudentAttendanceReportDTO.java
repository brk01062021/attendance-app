package com.school.attendance.dto;

import java.util.List;

public class StudentAttendanceReportDTO {

    private Long studentId;
    private String studentName;
    private String admissionNumber;
    private String rollNumber;
    private String className;
    private String section;
    private String fromDate;
    private String toDate;
    private String rangeType;
    private long totalWorkingDays;
    private long presentDays;
    private long absentDays;
    private long lateDays;
    private double attendancePercentage;
    private List<StudentDailyAttendanceDTO> dailyRecords;

    public StudentAttendanceReportDTO(Long studentId, String studentName, String admissionNumber,
                                      String rollNumber, String className, String section,
                                      String fromDate, String toDate, String rangeType,
                                      long totalWorkingDays, long presentDays, long absentDays,
                                      long lateDays, List<StudentDailyAttendanceDTO> dailyRecords) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.admissionNumber = admissionNumber;
        this.rollNumber = rollNumber;
        this.className = className;
        this.section = section;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.rangeType = rangeType;
        this.totalWorkingDays = totalWorkingDays;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.lateDays = lateDays;
        this.dailyRecords = dailyRecords;
        long attendedDays = presentDays + lateDays;
        this.attendancePercentage = totalWorkingDays == 0 ? 0 : (attendedDays * 100.0) / totalWorkingDays;
    }

    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getAdmissionNumber() { return admissionNumber; }
    public String getRollNumber() { return rollNumber; }
    public String getClassName() { return className; }
    public String getSection() { return section; }
    public String getFromDate() { return fromDate; }
    public String getToDate() { return toDate; }
    public String getRangeType() { return rangeType; }
    public long getTotalWorkingDays() { return totalWorkingDays; }
    public long getPresentDays() { return presentDays; }
    public long getAbsentDays() { return absentDays; }
    public long getLateDays() { return lateDays; }
    public double getAttendancePercentage() { return attendancePercentage; }
    public List<StudentDailyAttendanceDTO> getDailyRecords() { return dailyRecords; }
}
