package com.duoc.learningplatform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) que representa un objeto almacenado en AWS S3.
 *
 * ¿Por qué un DTO y no una Entity?
 * - Las clases @Entity (Usuario, Curso, Inscripcion) representan tablas en la base de datos
 *   y están vinculadas a JPA/Hibernate. S3 no es una base de datos relacional, es un
 *   almacenamiento de objetos (archivos), por lo que no se mapea con @Entity.
 * - Este DTO solo transporta información sobre los archivos que existen en S3
 *   (nombre, tamaño, fecha), sin persistirse en ninguna tabla.
 *
 * ¿Por qué estos 3 campos?
 * - Son los datos que AWS S3 devuelve al listar objetos de un bucket (s3Client.listObjectsV2).
 * - Es la información mínima necesaria para gestionar archivos: identificarlos (key),
 *   saber su peso (size) y cuándo fueron modificados (lastModified).
 * - Sin este DTO, tendríamos que devolver el objeto completo de AWS (S3Object) que contiene
 *   muchos datos internos innecesarios para el cliente.
 * - Es el equivalente a lo que ves en el explorador de archivos: nombre, tamaño y fecha.
 *
 * Se usa cuando listamos los objetos de un bucket con el endpoint GET /s3/{bucket},
 * para devolver una representación limpia de cada archivo al cliente (Postman).
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class S3ObjectDto {

    // El "key" es el nombre/ruta del archivo en S3 (ej: "1/resumen_inscripcion.txt")
    private String key;

    // Tamaño del archivo en bytes
    private Long size;

    // Fecha de última modificación del archivo en S3
    private String lastModified;
}
