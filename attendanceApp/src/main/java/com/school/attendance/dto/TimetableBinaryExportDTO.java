package com.school.attendance.dto;

public class TimetableBinaryExportDTO {
    private String batchId;
    private String format;
    private String fileName;
    private String contentType;
    private String base64Content;
    private Integer byteSize;
    private String message;

    public TimetableBinaryExportDTO() {}
    public TimetableBinaryExportDTO(String batchId, String format, String fileName, String contentType, String base64Content, Integer byteSize, String message) {
        this.batchId = batchId;
        this.format = format;
        this.fileName = fileName;
        this.contentType = contentType;
        this.base64Content = base64Content;
        this.byteSize = byteSize;
        this.message = message;
    }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getBase64Content() { return base64Content; }
    public void setBase64Content(String base64Content) { this.base64Content = base64Content; }
    public Integer getByteSize() { return byteSize; }
    public void setByteSize(Integer byteSize) { this.byteSize = byteSize; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
