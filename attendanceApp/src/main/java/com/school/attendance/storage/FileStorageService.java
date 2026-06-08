package com.school.attendance.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {

    public static final String MODULE_WORKBOOK_IMPORT = "workbook-import";
    public static final String MODULE_TIMETABLE_IMPORT = "timetable-import";
    public static final String MODULE_ATTENDANCE_RECOVERY = "attendance-recovery";

    private final StorageProvider storageProvider;

    public FileStorageService(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    public StoredFile uploadWorkbook(String schoolId, MultipartFile file, byte[] bytes) {
        return store(schoolId, MODULE_WORKBOOK_IMPORT, file, bytes);
    }

    public StoredFile uploadTimetableImport(String schoolId, MultipartFile file, byte[] bytes) {
        return store(schoolId, MODULE_TIMETABLE_IMPORT, file, bytes);
    }

    public StoredFile uploadAttendanceRecovery(String schoolId, MultipartFile file, byte[] bytes) {
        return store(schoolId, MODULE_ATTENDANCE_RECOVERY, file, bytes);
    }

    public StoredFile uploadWorkbook(String schoolId, MultipartFile file) throws IOException {
        return uploadWorkbook(schoolId, file, file.getBytes());
    }

    public StoredFile uploadTimetableImport(String schoolId, MultipartFile file) throws IOException {
        return uploadTimetableImport(schoolId, file, file.getBytes());
    }

    public StoredFile uploadAttendanceRecovery(String schoolId, MultipartFile file) throws IOException {
        return uploadAttendanceRecovery(schoolId, file, file.getBytes());
    }

    private StoredFile store(String schoolId, String module, MultipartFile file, byte[] bytes) {
        if (file == null || file.isEmpty() || bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Upload a valid file before validation.");
        }
        return storageProvider.store(new StorageUploadRequest(
                schoolId,
                module,
                file.getOriginalFilename(),
                file.getContentType() == null || file.getContentType().isBlank()
                        ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        : file.getContentType(),
                bytes
        ));
    }
}
