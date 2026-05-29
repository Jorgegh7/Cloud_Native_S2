package com.duoc.learningplatform.service.contrato;

import com.duoc.learningplatform.model.S3ObjectDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AwsS3Service {

    List<S3ObjectDto> listObjects(String bucket);
    byte[] downloadObjectAsBytes(String bucket, String key);
    void upload(String bucket, String key, MultipartFile file);
    void moveObject(String bucket, String sourceKey, String destKey);
    void deleteObject(String bucket, String key);

}
