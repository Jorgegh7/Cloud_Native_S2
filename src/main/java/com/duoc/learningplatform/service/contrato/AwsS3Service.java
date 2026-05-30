package com.duoc.learningplatform.service.contrato;

import com.duoc.learningplatform.model.S3ObjectDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interfaz que define las operaciones CRUD genéricas sobre archivos en AWS S3.
 *
 * ¿Por qué una interfaz?
 * - Sigue el patrón contrato/implementación usado en el resto del proyecto.
 * - Permite desacoplar la lógica: el controlador depende de la interfaz,
 *   no de la implementación concreta.
 *
 * Nota: Este servicio se creó para probar la conexión y el CRUD de archivos
 * en S3 de forma genérica (cualquier bucket, cualquier archivo).
 * Para el flujo real de la aplicación se usa InscripcionS3Service,
 * que trabaja específicamente con los resúmenes de inscripción.
 *
 * Conceptos clave:
 * - byte[]: arreglo de bytes, es la forma universal de representar cualquier archivo
 *   en Java (texto, PDF, imagen, etc). S3 entrega y recibe archivos como bytes.
 * - MultipartFile: clase de Spring que representa un archivo enviado via HTTP
 *   en formato multipart/form-data (como cuando subes un archivo desde Postman).
 *   Contiene el archivo empaquetado con su nombre (getOriginalFilename),
 *   tipo (getContentType), contenido (getBytes) y tamaño (getSize).
 *   Spring lo convierte automáticamente desde la petición HTTP gracias a
 *   la anotación @RequestParam("file") en el controlador.
 */

public interface AwsS3Service {

    // Lista todos los objetos (archivos) dentro de un bucket
    // Retorna una lista de S3ObjectDto con key, size y lastModified de cada archivo
    List<S3ObjectDto> listObjects(String bucket);

    // Descarga un archivo de S3 y lo retorna como arreglo de bytes
    // bucket: nombre del bucket, key: ruta/nombre del archivo en S3
    byte[] downloadObjectAsBytes(String bucket, String key);

    // Sube un archivo a S3
    // MultipartFile: es el archivo que llega desde Postman via form-data
    // En Postman se configura: Body → form-data → key: "file", tipo: File
    // Spring recibe ese paquete y lo convierte en MultipartFile automáticamente
    void upload(String bucket, String key, MultipartFile file);

    // Mueve un archivo dentro del mismo bucket (copia al destino + borra el original)
    // sourceKey: ruta actual, destKey: ruta nueva
    void moveObject(String bucket, String sourceKey, String destKey);

    // Elimina un archivo de S3
    void deleteObject(String bucket, String key);

}
