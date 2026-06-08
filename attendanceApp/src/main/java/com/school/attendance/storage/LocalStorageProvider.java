package com.school.attendance.storage;

import com.school.attendance.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "local", matchIfMissing = true)
public class LocalStorageProvider implements StorageProvider {

    private final AppProperties appProperties;

    public LocalStorageProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public StoredFile store(StorageUploadRequest request) {
        try {
            String key = StorageKeyFactory.buildKey(appProperties.getEnv(), request.schoolId(), request.module(), request.originalFilename());
            Path root = Paths.get(appProperties.getStorage().getLocalUploadDir()).toAbsolutePath().normalize();
            Path target = root.resolve(key).normalize();
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("Invalid upload path.");
            }
            Files.createDirectories(target.getParent());
            Files.write(target, request.bytes());
            return new StoredFile("local", null, key, request.originalFilename(), request.contentType(), request.bytes().length);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store uploaded file locally.", ex);
        }
    }

    @Override
    public byte[] read(String storageKey) {
        try {
            Path root = Paths.get(appProperties.getStorage().getLocalUploadDir()).toAbsolutePath().normalize();
            Path target = root.resolve(storageKey).normalize();
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("Invalid storage key.");
            }
            return Files.readAllBytes(target);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read stored local file.", ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path root = Paths.get(appProperties.getStorage().getLocalUploadDir()).toAbsolutePath().normalize();
            Path target = root.resolve(storageKey).normalize();
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("Invalid storage key.");
            }
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to delete stored local file.", ex);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        Path root = Paths.get(appProperties.getStorage().getLocalUploadDir()).toAbsolutePath().normalize();
        Path target = root.resolve(storageKey).normalize();
        return target.startsWith(root) && Files.exists(target);
    }
}
