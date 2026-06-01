package com.school.attendance.repository;

import java.time.LocalDateTime;

public interface SchoolImportUploadSummaryProjection {
    Long getId();
    String getSchoolCode();
    String getAcademicYear();
    String getImportType();
    String getFileName();
    String getStatus();
    String getImportBatchId();
    int getTotalRows();
    int getTotalSheets();
    int getErrorCount();
    int getWarningCount();
    boolean isCommitted();
    boolean isRolledBack();
    int getStagedRowCount();
    String getLifecycleMessage();
    String getUploadedByRole();
    LocalDateTime getUploadedAt();
    LocalDateTime getCommittedAt();
    LocalDateTime getRolledBackAt();
}
