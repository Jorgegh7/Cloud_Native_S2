package com.duoc.learningplatform.controller;

import com.duoc.learningplatform.model.S3ObjectDto;
import com.duoc.learningplatform.service.contrato.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.util.List;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;
    private final S3Client s3Client;

    // Test de conexión
    @GetMapping("/test")
    public ResponseEntity<List<String>> testConnection() {
        List<String> buckets = s3Client.listBuckets().buckets()
                .stream()
                .map(Bucket::name)
                .toList();
        return ResponseEntity.ok(buckets);
    }

    // Listar objetos en un bucket
    @GetMapping("/{bucket}")
    public ResponseEntity<List<S3ObjectDto>> listObjects(@PathVariable String bucket) {
        List<S3ObjectDto> dtoList = awsS3Service.listObjects(bucket);
        return ResponseEntity.ok(dtoList);
    }

    // Descargar archivo
    @GetMapping("/{bucket}/object")
    public ResponseEntity<byte[]> downloadObject(@PathVariable String bucket, @RequestParam String key) {
        byte[] fileBytes = awsS3Service.downloadObjectAsBytes(bucket, key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + key)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);
    }

    // Subir archivo
    @PostMapping("/{bucket}/object")
    public ResponseEntity<Void> uploadObject(@PathVariable String bucket, @RequestParam String key,
                                             @RequestParam("file") MultipartFile file) {
        awsS3Service.upload(bucket, key, file);
        return ResponseEntity.ok().build();
    }

    // Mover objeto dentro del mismo bucket
    @PostMapping("/{bucket}/move")
    public ResponseEntity<Void> moveObject(@PathVariable String bucket, @RequestParam String sourceKey,
                                           @RequestParam String destKey) {
        awsS3Service.moveObject(bucket, sourceKey, destKey);
        return ResponseEntity.ok().build();
    }

    // Borrar objeto
    @DeleteMapping("/{bucket}/object")
    public ResponseEntity<Void> deleteObject(@PathVariable String bucket, @RequestParam String key) {
        awsS3Service.deleteObject(bucket, key);
        return ResponseEntity.noContent().build();
    }
}