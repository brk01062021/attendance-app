package com.school.attendance.storage;

import com.school.attendance.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@ConditionalOnProperty(name = "app.storage.mode", havingValue = "s3")
public class S3StorageProvider implements StorageProvider {

    private final AppProperties appProperties;
    private final S3Client s3Client;

    public S3StorageProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.s3Client = S3Client.builder()
                .region(Region.of(required(appProperties.getAws().getRegion(), "AWS region is required for S3 storage.")))
                .build();
    }

    @Override
    public StoredFile store(StorageUploadRequest request) {
        String bucket = required(appProperties.getAws().getS3UploadBucket(), "S3 upload bucket is required.");
        String key = StorageKeyFactory.buildKey(appProperties.getEnv(), request.schoolId(), request.module(), request.originalFilename());
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(request.contentType())
                .contentLength((long) request.bytes().length)
                .build();
        s3Client.putObject(putRequest, RequestBody.fromBytes(request.bytes()));
        return new StoredFile("s3", bucket, key, request.originalFilename(), request.contentType(), request.bytes().length);
    }

    @Override
    public byte[] read(String storageKey) {
        String bucket = required(appProperties.getAws().getS3UploadBucket(), "S3 upload bucket is required.");
        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build());
        return objectBytes.asByteArray();
    }

    @Override
    public void delete(String storageKey) {
        String bucket = required(appProperties.getAws().getS3UploadBucket(), "S3 upload bucket is required.");
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(storageKey).build());
    }

    @Override
    public boolean exists(String storageKey) {
        String bucket = required(appProperties.getAws().getS3UploadBucket(), "S3 upload bucket is required.");
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(storageKey).build());
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw ex;
        }
    }

    private static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value.trim();
    }
}
