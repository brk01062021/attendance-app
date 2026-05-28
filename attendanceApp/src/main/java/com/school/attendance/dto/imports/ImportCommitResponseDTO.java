package com.school.attendance.dto.imports;

import java.time.LocalDateTime;

public class ImportCommitResponseDTO {
    private Long uploadId;
    private String schoolId;
    private String status;
    private String importBatchId;
    private String message;
    private boolean committed;
    private boolean rolledBack;
    private int stagedRowCount;
    private String lifecycleMessage;
    private LocalDateTime actionAt;

    public Long getUploadId() { return uploadId; }
    public void setUploadId(Long uploadId) { this.uploadId = uploadId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImportBatchId() { return importBatchId; }
    public void setImportBatchId(String importBatchId) { this.importBatchId = importBatchId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isCommitted() { return committed; }
    public void setCommitted(boolean committed) { this.committed = committed; }
    public boolean isRolledBack() { return rolledBack; }
    public void setRolledBack(boolean rolledBack) { this.rolledBack = rolledBack; }
    public int getStagedRowCount() { return stagedRowCount; }
    public void setStagedRowCount(int stagedRowCount) { this.stagedRowCount = stagedRowCount; }
    public String getLifecycleMessage() { return lifecycleMessage; }
    public void setLifecycleMessage(String lifecycleMessage) { this.lifecycleMessage = lifecycleMessage; }
    public LocalDateTime getActionAt() { return actionAt; }
    public void setActionAt(LocalDateTime actionAt) { this.actionAt = actionAt; }
}
