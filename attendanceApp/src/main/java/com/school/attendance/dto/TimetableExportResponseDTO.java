package com.school.attendance.dto;

public class TimetableExportResponseDTO {
    private String batchId;
    private String format;
    private String fileName;
    private String contentType;
    private String content;

    public TimetableExportResponseDTO() {}
    public TimetableExportResponseDTO(String batchId, String format, String fileName, String contentType, String content) {
        this.batchId = batchId; this.format = format; this.fileName = fileName; this.contentType = contentType; this.content = content;
    }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
