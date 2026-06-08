package com.school.attendance.dto;

import com.school.attendance.entity.UploadedFile;

import java.time.LocalDateTime;

public record UploadedFileHistoryDTO(
        Long id,
        String schoolId,
        String module,
        String storageProvider,
        String bucket,
        String storageKey,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        String uploadedBy,
        String status,
        LocalDateTime createdAt
) {
    public static UploadedFileHistoryDTO from(UploadedFile uploadedFile) {
        return new UploadedFileHistoryDTO(
                uploadedFile.getId(),
                uploadedFile.getSchoolId(),
                uploadedFile.getModule(),
                uploadedFile.getStorageProvider(),
                uploadedFile.getBucket(),
                uploadedFile.getStorageKey(),
                uploadedFile.getOriginalFilename(),
                uploadedFile.getContentType(),
                uploadedFile.getSizeBytes(),
                uploadedFile.getUploadedBy(),
                uploadedFile.getStatus(),
                uploadedFile.getCreatedAt()
        );
    }
}