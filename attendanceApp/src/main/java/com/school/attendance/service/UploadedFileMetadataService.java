package com.school.attendance.service;

import com.school.attendance.entity.UploadedFile;
import com.school.attendance.repository.UploadedFileRepository;
import com.school.attendance.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UploadedFileMetadataService {

    private final UploadedFileRepository uploadedFileRepository;

    public UploadedFile save(
            String schoolId,
            String module,
            StoredFile storedFile,
            String uploadedBy,
            String status
    ) {
        UploadedFile uploadedFile = UploadedFile.builder()
                .schoolId(schoolId)
                .module(module)
                .storageProvider(storedFile.storageProvider())
                .bucket(storedFile.bucket())
                .storageKey(storedFile.storageKey())
                .originalFilename(storedFile.originalFilename())
                .contentType(storedFile.contentType())
                .sizeBytes(storedFile.sizeBytes())
                .uploadedBy(uploadedBy)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        return uploadedFileRepository.save(uploadedFile);
    }
}