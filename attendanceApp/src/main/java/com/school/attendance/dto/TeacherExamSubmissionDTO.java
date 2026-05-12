package com.school.attendance.dto;

import java.time.LocalDate;

public class TeacherExamSubmissionDTO {
    private LocalDate examDate;
    private String examName;
    private String className;
    private String section;
    private String subjectName;
    private Integer totalResultsSubmitted;

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public Integer getTotalResultsSubmitted() { return totalResultsSubmitted; }
    public void setTotalResultsSubmitted(Integer totalResultsSubmitted) { this.totalResultsSubmitted = totalResultsSubmitted; }
}
