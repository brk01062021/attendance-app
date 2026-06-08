package com.school.attendance.storage;

public record StoredFile(
        String storageProvider,
        String bucket,
        String storageKey,
        String originalFilename,
        String contentType,
        long sizeBytes
) {
}
