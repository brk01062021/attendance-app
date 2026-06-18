package com.school.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_reminder_uploads", indexes = {@Index(name = "idx_fee_upload_school_created", columnList = "schoolId,createdAt")})
public class FeeReminderUpload {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 4) private String schoolId;
    private String originalFilename; private String uploadedBy; private String status = "PREVIEW_READY";
    private int totalRows; private int readyRows; private int invalidRows; private int missingStudentRows; private int missingParentMappingRows; private int sentRows; private int failedRows;
    private LocalDateTime createdAt = LocalDateTime.now(); private LocalDateTime sentAt;
    public Long getId(){return id;} public String getSchoolId(){return schoolId;} public void setSchoolId(String v){schoolId=v;}
    public String getOriginalFilename(){return originalFilename;} public void setOriginalFilename(String v){originalFilename=v;} public String getUploadedBy(){return uploadedBy;} public void setUploadedBy(String v){uploadedBy=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;} public int getTotalRows(){return totalRows;} public void setTotalRows(int v){totalRows=v;}
    public int getReadyRows(){return readyRows;} public void setReadyRows(int v){readyRows=v;} public int getInvalidRows(){return invalidRows;} public void setInvalidRows(int v){invalidRows=v;}
    public int getMissingStudentRows(){return missingStudentRows;} public void setMissingStudentRows(int v){missingStudentRows=v;} public int getMissingParentMappingRows(){return missingParentMappingRows;} public void setMissingParentMappingRows(int v){missingParentMappingRows=v;}
    public int getSentRows(){return sentRows;} public void setSentRows(int v){sentRows=v;} public int getFailedRows(){return failedRows;} public void setFailedRows(int v){failedRows=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;} public LocalDateTime getSentAt(){return sentAt;} public void setSentAt(LocalDateTime v){sentAt=v;}
}
