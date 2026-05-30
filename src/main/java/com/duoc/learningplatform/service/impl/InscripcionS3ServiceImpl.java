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

/**
 * Implementación del servicio que gestiona los resúmenes de inscripción en AWS S3.
 *
 * Esta clase es el corazón de la integración entre la base de datos y S3.
 * Combina datos de la BD (inscripción, estudiante, cursos) para generar
 * un archivo de resumen y gestionarlo en el bucket de S3.
 *
 * Dependencias inyectadas:
 * - S3Client: cliente de AWS para operar sobre S3 (auto-configurado por el starter)
 * - InscripcionRepository: para buscar inscripciones en la BD
 * - InscripcionService: para actualizar inscripciones en la BD (usado en modificar)
 *
 * @Service: marca esta clase como un bean de servicio que Spring gestiona
 * @RequiredArgsConstructor: Lombok genera el constructor con los 3 campos "final",
 *   permitiendo que Spring inyecte las dependencias automáticamente
 */

@Service
@RequiredArgsConstructor
public class InscripcionS3ServiceImpl implements InscripcionS3Service {

    // Cliente de AWS S3 (auto-configurado por spring-cloud-aws-starter-s3)
    // Se usa para todas las operaciones S3: subir, descargar, borrar
    private final S3Client s3Client;

    // Repositorio JPA para buscar inscripciones en la base de datos H2
    // Se usa en generarResumen() para obtener los datos de la inscripción
    private final InscripcionRepository inscripcionRepository;

    // Servicio de inscripciones de la Semana 1
    // Se usa en modificarResumen() para actualizar la inscripción en la BD
    // antes de regenerar el resumen
    private final InscripcionService inscripcionService;

    // @Value inyecta el valor de la propiedad "aws.s3.bucket" desde application.properties
    // En nuestro caso: "mslpbucket"
    // Así no hardcodeamos el nombre del bucket en cada métod0
    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * Genera la ruta (key) del archivo en S3 para una inscripción.
     * Ejemplo: inscripcionId = 1 → "1/resumen_inscripcion.txt"
     *
     * El "/" crea una "carpeta" virtual en S3 con el número de la inscripción,
     * cumpliendo el requerimiento de que cada resumen esté en su propia carpeta.
     *
     * Este métod0 es privado porque solo lo usan los demás métodos de esta clase.
     */
    private String generarKey(Long inscripcionId) {
        return inscripcionId + "/resumen_inscripcion.txt";
    }

    /**
     * Genera el contenido del resumen a partir de los datos en la BD.
     * NO sube nada a S3, solo crea el texto en memoria como byte[].
     *
     * Flujo:
     * 1. Busca la inscripción en la BD por su ID
     * 2. Construye un texto con los datos (estudiante, fecha, cursos, total)
     * 3. Convierte el texto a byte[] para que pueda enviarse como archivo
     *
     * .orElseThrow(): si no encuentra la inscripción, lanza una excepción
     * en vez de retornar null (evita NullPointerException).
     *
     * StringBuilder: clase de Java para concatenar strings de forma eficiente.
     * Es mejor que usar "+" cuando hay muchas concatenaciones.
     *
     * .getBytes(StandardCharsets.UTF_8): convierte el String a byte[] usando
     * codificación UTF-8, que soporta caracteres especiales (tildes, ñ, etc).
     */
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

    /**
     * Genera el resumen y lo sube a S3.
     *
     * Flujo:
     * 1. Llama a generarResumen() para crear el contenido como byte[]
     * 2. Genera la ruta en S3 con generarKey() (ej: "1/resumen_inscripcion.txt")
     * 3. Sube el archivo a S3 usando putObject()
     *
     * PutObjectRequest.builder(): construye la petición de subida con:
     *   - .bucket(bucket): en qué bucket guardar ("mslpbucket")
     *   - .key(key): la ruta/nombre del archivo en S3
     *   - .contentType("text/plain"): indica que es un archivo de texto
     *
     * RequestBody.fromBytes(resumen): convierte el byte[] al formato que
     * el SDK de AWS necesita para enviar el archivo. Es el "empaquetado"
     * del contenido para que viaje a S3.
     *
     * Si el archivo ya existe en S3 con el mismo key, se sobreescribe.
     */
    @Override
    public void subirResumen(Long inscripcionId) {
        byte[] resumen = generarResumen(inscripcionId);
        String key = generarKey(inscripcionId);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)     // Bucket destino: "mslpbucket"
                        .key(key)           // Ruta del archivo en S3
                        .contentType("text/plain") // Tipo de archivo: texto plano
                        .build(),
                RequestBody.fromBytes(resumen)  // El contenido del archivo como bytes
        );
    }

    /**
     * Descarga el resumen que ya está almacenado en S3.
     *
     * A diferencia de generarResumen() que crea el archivo desde la BD,
     * este métod baja el archivo que ya existe en S3.
     *
     * GetObjectRequest: petición para obtener un objeto de S3.
     * .getObjectAsBytes(): descarga el archivo completo como bytes.
     * .asByteArray(): convierte a byte[] para enviarlo al cliente.
     *
     * Si el archivo no existe en S3, lanza NoSuchKeyException.
     */
    @Override
    public byte[] descargarResumen(Long inscripcionId) {
        String key = generarKey(inscripcionId);
        return s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build()
        ).asByteArray();
    }

    /**
     * Modifica la inscripción en la BD y regenera el resumen en S3.
     *
     * Este es el único métod que combina ambos mundos:
     * - Paso 1: actualiza los datos en la base de datos (BD)
     * - Paso 2: regenera el resumen con los datos nuevos y lo sube a S3
     *
     * Así se mantiene la consistencia entre la BD y S3.
     * El archivo anterior en S3 se sobreescribe automáticamente
     * porque tiene el mismo key.
     *
     * inscripcionService.update(): métod de la Semana 1 que actualiza
     * estudiante, cursos y totalPagar en la BD.
     *
     * subirResumen(): genera el resumen con los datos YA actualizados
     * y lo sube a S3, sobreescribiendo el anterior.
     */
    @Override
    public void modificarResumen(Long inscripcionId, Inscripcion inscripcion) {
        // 1. Actualizar en BD
        inscripcionService.update(inscripcionId, inscripcion);
        // 2. Regenerar resumen en S3
        subirResumen(inscripcionId);
    }

    /**
     * Elimina el resumen de S3.
     *
     * Solo borra el archivo en S3, NO elimina la inscripción de la BD.
     * Si se necesita el resumen de nuevo, se puede regenerar con subirResumen().
     *
     * DeleteObjectRequest: petición para eliminar un objeto de S3.
     * Si el archivo no existe, S3 no lanza error (es idempotente).
     */
    @Override
    public void borrarResumen(Long inscripcionId) {
        String key = generarKey(inscripcionId);
        s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucket).key(key).build()
        );
    }
}