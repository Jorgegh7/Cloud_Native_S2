package com.duoc.learningplatform.service.impl;

import com.duoc.learningplatform.model.Curso;
import com.duoc.learningplatform.model.Inscripcion;
import com.duoc.learningplatform.repository.InscripcionRepository;
import com.duoc.learningplatform.service.contrato.InscripcionS3Service;
import com.duoc.learningplatform.service.contrato.InscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

@Service
@RequiredArgsConstructor
public class InscripcionS3ServiceImpl implements InscripcionS3Service {

    private final S3Client s3Client;
    private final InscripcionRepository inscripcionRepository;
    private final InscripcionService inscripcionService;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private String generarKey(Long inscripcionId) {
        return inscripcionId + "/resumen_inscripcion.txt";
    }

    @Override
    public byte[] generarResumen(Long inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("     RESUMEN DE INSCRIPCIÓN #").append(inscripcion.getId()).append("\n");
        sb.append("========================================\n\n");
        sb.append("Estudiante: ").append(inscripcion.getEstudiante().getNombre()).append("\n");
        sb.append("Fecha: ").append(sdf.format(inscripcion.getFechaInscripcion())).append("\n\n");
        sb.append("Cursos inscritos:\n");
        for (Curso curso : inscripcion.getCursos()) {
            sb.append("  - ").append(curso.getNombre()).append("\n");
        }
        sb.append("\nTotal a pagar: $").append(inscripcion.getTotalPagar()).append("\n");
        sb.append("========================================\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void subirResumen(Long inscripcionId) {
        byte[] resumen = generarResumen(inscripcionId);
        String key = generarKey(inscripcionId);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("text/plain")
                        .build(),
                RequestBody.fromBytes(resumen)
        );
    }

    @Override
    public byte[] descargarResumen(Long inscripcionId) {
        String key = generarKey(inscripcionId);
        return s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build()
        ).asByteArray();
    }

    @Override
    public void modificarResumen(Long inscripcionId, Inscripcion inscripcion) {
        // 1. Actualizar en BD
        inscripcionService.update(inscripcionId, inscripcion);
        // 2. Regenerar resumen en S3
        subirResumen(inscripcionId);
    }

    @Override
    public void borrarResumen(Long inscripcionId) {
        String key = generarKey(inscripcionId);
        s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucket).key(key).build()
        );
    }
}