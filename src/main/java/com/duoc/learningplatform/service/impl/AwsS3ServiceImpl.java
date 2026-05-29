package com.duoc.learningplatform.service.impl;

import com.duoc.learningplatform.model.S3ObjectDto;
import com.duoc.learningplatform.service.contrato.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AwsS3ServiceImpl implements AwsS3Service {

    private final S3Client s3Client;

    @Override
    public List<S3ObjectDto> listObjects(String bucket) {
        ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder().bucket(bucket).build()
        );
        return response.contents().stream()
                .map(obj -> new S3ObjectDto(obj.key(), obj.size(), obj.lastModified().toString()))
                .toList();
    }

    @Override
    public byte[] downloadObjectAsBytes(String bucket, String key) {
        return s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build()
        ).asByteArray();
    }

    @Override
    public void upload(String bucket, String key, MultipartFile file) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new RuntimeException("Error al subir archivo", e);
        }
    }

    @Override
    public void moveObject(String bucket, String sourceKey, String destKey) {
        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(sourceKey)
                .destinationBucket(bucket)
                .destinationKey(destKey)
                .build());
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(sourceKey)
                .build());
    }

    @Override
    public void deleteObject(String bucket, String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucket).key(key).build()
        );
    }
}
