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

/**
 * Controlador REST genérico para operaciones CRUD sobre archivos en AWS S3.
 *
 * Este controlador fue creado para probar la conexión y funcionalidad de S3
 * antes de integrar con el modelo de Inscripción. No es parte del flujo
 * principal de la aplicación pero es útil para demostrar que la conexión funciona.
 *
 * Todos los endpoints reciben el nombre del bucket como @PathVariable,
 * lo que permite operar sobre cualquier bucket sin hardcodear el nombre.
 *
 * Ruta base: /s3
 *
 * @RequiredArgsConstructor: Lombok genera el constructor con los campos "final",
 * que es la forma en que Spring inyecta las dependencias (AwsS3Service y S3Client).
 */

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    /** S3Client es una clase del SDK de AWS (software.amazon.awssdk.services.s3.S3Client)
    * Es el cliente que se comunica directamente con los servicios de AWS S3.
    *
    * ¿De dónde viene si o ha sido creada?
    * Al agregar spring-cloud-aws-starter-s3 en el pom.xml, Spring Cloud AWS
    * auto-configura un bean S3Client al arrancar la aplicación. Internamente hace:
    *   S3Client.builder()
    *       .region(...)              → lee de spring.cloud.aws.region.static
    *       .credentialsProvider(...) → lee de las variables de entorno AWS_*
    *       .build();
    *
    * No vemos ese código porque el starter lo hace automáticamente.
    * Lee las variables de entorno (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY,
    * AWS_SESSION_TOKEN) y la región de application.properties.
    *
    * Cuando escribimos "private final S3Client s3Client", Spring detecta que
    * alguien necesita un S3Client, encuentra el bean que el starter creó,
    * y lo inyecta automáticamente. Es inyección de dependencias, igual que
    * con los repositorios y servicios, pero el bean lo crea el starter.
    */

    private final S3Client s3Client;

    /**
     * Test de conexión con AWS S3.
     * GET /s3/test
     *
     * Lista todos los buckets de la cuenta AWS para verificar que
     * las credenciales y la conexión funcionan correctamente.
     *
     * s3Client.listBuckets() → pide a AWS la lista de buckets
     * .buckets() → extrae la lista de objetos Bucket
     * .stream().map(Bucket::name) → de cada Bucket saca solo el nombre
     * .toList() → convierte el stream a una lista de Strings
     *
     * Respuesta exitosa: 200 OK con ["mslpbucket"]
     */
    @GetMapping("/test")
    public ResponseEntity<List<String>> testConnection() {
        List<String> buckets = s3Client.listBuckets().buckets()
                .stream()
                .map(Bucket::name)
                .toList();
        return ResponseEntity.ok(buckets);
    }

    /**
     * Listar todos los archivos de un bucket.
     * GET /s3/{bucket}
     * Ejemplo: GET /s3/mslpbucket
     *
     * @PathVariable: extrae "mslpbucket" de la URL y lo asigna a la variable bucket.
     * Retorna una lista de S3ObjectDto con key, size y lastModified de cada archivo.
     */
    @GetMapping("/{bucket}")
    public ResponseEntity<List<S3ObjectDto>> listObjects(@PathVariable String bucket) {
        List<S3ObjectDto> dtoList = awsS3Service.listObjects(bucket);
        return ResponseEntity.ok(dtoList);
    }

    /**
     * Descargar un archivo desde S3.
     * GET /s3/{bucket}/object?key=test.txt
     *
     * @RequestParam: extrae el valor de "key" del query parameter (?key=test.txt).
     *
     * HttpHeaders.CONTENT_DISPOSITION: cabecera HTTP que le dice al navegador/Postman
     * que la respuesta es un archivo descargable con el nombre indicado.
     * "attachment; filename=test.txt" → fuerza la descarga con ese nombre.
     *
     * MediaType.APPLICATION_OCTET_STREAM: tipo de contenido genérico para archivos binarios.
     * Indica que la respuesta es un archivo (no JSON ni texto).
     *
     * El archivo viaja como byte[] en el body de la respuesta.
     */
    @GetMapping("/{bucket}/object")
    public ResponseEntity<byte[]> downloadObject(@PathVariable String bucket, @RequestParam String key) {
        byte[] fileBytes = awsS3Service.downloadObjectAsBytes(bucket, key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + key)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);
    }

    /**
     * Subir un archivo a S3.
     * POST /s3/{bucket}/object?key=test.txt
     * Body: form-data con key="file" y tipo File
     *
     * @RequestParam String key: el nombre/ruta con que se guardará en S3.
     * @RequestParam("file") MultipartFile file: el archivo enviado desde Postman.
     *   - "file" debe coincidir con el key en form-data de Postman.
     *   - Spring convierte automáticamente el archivo HTTP en un objeto MultipartFile.
     *
     * ResponseEntity<Void>: responde 200 OK sin body (solo confirmación de éxito).
     * .ok().build() construye una respuesta vacía con status 200.
     */
    @PostMapping("/{bucket}/object")
    public ResponseEntity<Void> uploadObject(@PathVariable String bucket, @RequestParam String key,
                                             @RequestParam("file") MultipartFile file) {
        awsS3Service.upload(bucket, key, file);
        return ResponseEntity.ok().build();
    }

    /**
     * Mover/renombrar un archivo dentro del mismo bucket.
     * POST /s3/{bucket}/move?sourceKey=test.txt&destKey=carpeta/test.txt
     *
     * S3 no tiene operación "mover", así que internamente:
     * 1. Copia el archivo al destino nuevo (destKey)
     * 2. Borra el original (sourceKey)
     *
     * Útil para reorganizar archivos dentro del bucket o renombrarlos.
     */
    @PostMapping("/{bucket}/move")
    public ResponseEntity<Void> moveObject(@PathVariable String bucket, @RequestParam String sourceKey,
                                           @RequestParam String destKey) {
        awsS3Service.moveObject(bucket, sourceKey, destKey);
        return ResponseEntity.ok().build();
    }

    /**
     * Borrar un archivo de S3.
     * DELETE /s3/{bucket}/object?key=test.txt
     *
     * ResponseEntity.noContent(): responde con 204 No Content,
     * que es el código HTTP estándar para "eliminado exitosamente, no hay contenido que devolver".
     * A diferencia de 200 OK, el 204 indica explícitamente que el recurso ya no existe.
     */
    @DeleteMapping("/{bucket}/object")
    public ResponseEntity<Void> deleteObject(@PathVariable String bucket, @RequestParam String key) {
        awsS3Service.deleteObject(bucket, key);
        return ResponseEntity.noContent().build();
    }
}