package com.school.attendance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_reminder_history", indexes = {@Index(name = "idx_fee_history_school_sent", columnList = "schoolId,sentAt"), @Index(name = "idx_fee_history_parent", columnList = "parentUserId")})
public class FeeReminderHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long uploadId; private Long rowId; @Column(nullable=false,length=4) private String schoolId; private Long studentDbId; private String studentId; private String studentName; private String className; private String section;
    private Long parentUserId; private String parentName; private BigDecimal pendingAmount; private LocalDate dueDate; @Column(columnDefinition="TEXT") private String remarks; private String status="SENT"; private String channel="IN_APP"; private LocalDateTime sentAt=LocalDateTime.now();
    public Long getId(){return id;} public Long getUploadId(){return uploadId;} public void setUploadId(Long v){uploadId=v;} public Long getRowId(){return rowId;} public void setRowId(Long v){rowId=v;} public String getSchoolId(){return schoolId;} public void setSchoolId(String v){schoolId=v;}
    public Long getStudentDbId(){return studentDbId;} public void setStudentDbId(Long v){studentDbId=v;} public String getStudentId(){return studentId;} public void setStudentId(String v){studentId=v;} public String getStudentName(){return studentName;} public void setStudentName(String v){studentName=v;}
    public String getClassName(){return className;} public void setClassName(String v){className=v;} public String getSection(){return section;} public void setSection(String v){section=v;} public Long getParentUserId(){return parentUserId;} public void setParentUserId(Long v){parentUserId=v;}
    public String getParentName(){return parentName;} public void setParentName(String v){parentName=v;} public BigDecimal getPendingAmount(){return pendingAmount;} public void setPendingAmount(BigDecimal v){pendingAmount=v;} public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate v){dueDate=v;}
    public String getRemarks(){return remarks;} public void setRemarks(String v){remarks=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getChannel(){return channel;} public void setChannel(String v){channel=v;} public LocalDateTime getSentAt(){return sentAt;} public void setSentAt(LocalDateTime v){sentAt=v;}
}
