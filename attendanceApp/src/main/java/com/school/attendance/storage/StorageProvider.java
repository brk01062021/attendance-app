package com.school.attendance.storage;

public interface StorageProvider {
    StoredFile store(StorageUploadRequest request);

    byte[] read(String storageKey);

    void delete(String storageKey);

    boolean exists(String storageKey);
}
