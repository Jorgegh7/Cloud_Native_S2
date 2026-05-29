package com.duoc.learningplatform.controller;

import com.duoc.learningplatform.model.Inscripcion;
import com.duoc.learningplatform.service.contrato.InscripcionS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/inscripciones")
@RequiredArgsConstructor
public class InscripcionS3Controller {

    private final InscripcionS3Service inscripcionS3Service;

    // Generar y descargar resumen localmente
    @GetMapping("/{id}/resumen/generar")
    public ResponseEntity<byte[]> generarResumen(@PathVariable Long id) {
        byte[] resumen = inscripcionS3Service.generarResumen(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resumen_inscripcion_" + id + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resumen);
    }

    // Subir resumen a S3
    @PostMapping("/{id}/resumen/subir")
    public ResponseEntity<String> subirResumen(@PathVariable Long id) {
        inscripcionS3Service.subirResumen(id);
        return ResponseEntity.ok("Resumen de inscripción #" + id + " subido a S3");
    }

    // Descargar resumen desde S3
    @GetMapping("/{id}/resumen/descargar")
    public ResponseEntity<byte[]> descargarResumen(@PathVariable Long id) {
        byte[] resumen = inscripcionS3Service.descargarResumen(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resumen_inscripcion_" + id + ".txt")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resumen);
    }

    @PutMapping("/{id}/resumen/modificar")
    public ResponseEntity<String> modificarResumen(@PathVariable Long id, @RequestBody Inscripcion inscripcion) {
        inscripcionS3Service.modificarResumen(id, inscripcion);
        return ResponseEntity.ok("Inscripción #" + id + " actualizada y resumen regenerado en S3");
    }

    // Borrar resumen de S3
    @DeleteMapping("/{id}/resumen/borrar")
    public ResponseEntity<String> borrarResumen(@PathVariable Long id) {
        inscripcionS3Service.borrarResumen(id);
        return ResponseEntity.ok("Resumen de inscripción #" + id + " borrado de S3");
    }
}