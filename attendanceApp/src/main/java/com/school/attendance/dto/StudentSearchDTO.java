package com.school.attendance.dto;

public class StudentSearchDTO {

    private Long studentId;
    private String studentName;
    private String admissionNumber;
    private String rollNumber;
    private String className;
    private String section;

    public StudentSearchDTO(Long studentId, String studentName, String admissionNumber,
                            String rollNumber, String className, String section) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.admissionNumber = admissionNumber;
        this.rollNumber = rollNumber;
        this.className = className;
        this.section = section;
    }

    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getAdmissionNumber() { return admissionNumber; }
    public String getRollNumber() { return rollNumber; }
    public String getClassName() { return className; }
    public String getSection() { return section; }
}
