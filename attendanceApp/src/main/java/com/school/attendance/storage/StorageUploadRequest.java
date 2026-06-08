package com.school.attendance.storage;

public record StorageUploadRequest(
        String schoolId,
        String module,
        String originalFilename,
        String contentType,
        byte[] bytes
) {
}
