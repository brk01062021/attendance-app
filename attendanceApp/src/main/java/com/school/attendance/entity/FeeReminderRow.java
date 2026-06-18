package com.school.attendance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_reminder_rows", indexes = {@Index(name = "idx_fee_row_upload", columnList = "uploadId"), @Index(name = "idx_fee_row_status", columnList = "status")})
public class FeeReminderRow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long uploadId; @Column(nullable = false, length = 4) private String schoolId;
    private int rowNumber; private String studentId; private String studentName; private String className; private String section; private BigDecimal pendingAmount; private LocalDate dueDate;
    @Column(columnDefinition = "TEXT") private String remarks; private Long mappedStudentDbId; @Column(columnDefinition = "TEXT") private String mappedParentUserIds; @Column(columnDefinition = "TEXT") private String mappedParentNames;
    private String status; @Column(columnDefinition = "TEXT") private String validationMessage; private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId(){return id;} public Long getUploadId(){return uploadId;} public void setUploadId(Long v){uploadId=v;} public String getSchoolId(){return schoolId;} public void setSchoolId(String v){schoolId=v;}
    public int getRowNumber(){return rowNumber;} public void setRowNumber(int v){rowNumber=v;} public String getStudentId(){return studentId;} public void setStudentId(String v){studentId=v;} public String getStudentName(){return studentName;} public void setStudentName(String v){studentName=v;}
    public String getClassName(){return className;} public void setClassName(String v){className=v;} public String getSection(){return section;} public void setSection(String v){section=v;} public BigDecimal getPendingAmount(){return pendingAmount;} public void setPendingAmount(BigDecimal v){pendingAmount=v;}
    public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate v){dueDate=v;} public String getRemarks(){return remarks;} public void setRemarks(String v){remarks=v;} public Long getMappedStudentDbId(){return mappedStudentDbId;} public void setMappedStudentDbId(Long v){mappedStudentDbId=v;}
    public String getMappedParentUserIds(){return mappedParentUserIds;} public void setMappedParentUserIds(String v){mappedParentUserIds=v;} public String getMappedParentNames(){return mappedParentNames;} public void setMappedParentNames(String v){mappedParentNames=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getValidationMessage(){return validationMessage;} public void setValidationMessage(String v){validationMessage=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
