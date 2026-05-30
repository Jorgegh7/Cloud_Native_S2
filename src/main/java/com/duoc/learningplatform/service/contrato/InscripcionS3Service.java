package com.duoc.learningplatform.service.contrato;

import com.duoc.learningplatform.model.Inscripcion;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interfaz que define las operaciones para gestionar los resúmenes
 * de inscripción en AWS S3.
 *
 * Este es el servicio principal de la Semana 2. A diferencia de AwsS3Service
 * (que es genérico para cualquier archivo), este servicio trabaja específicamente
 * con los resúmenes de inscripción vinculados al modelo de datos de la aplicación.
 *
 * Flujo principal:
 * 1. Se crea una inscripción en la BD (estudiante + cursos + total)
 * 2. Se genera un resumen en texto con los datos de la inscripción
 * 3. Se sube a S3 en la carpeta {id}/resumen_inscripcion.txt
 * 4. Se puede descargar, modificar (actualizar datos + regenerar) o borrar
 *
 * Todos los métodos reciben inscripcionId porque es la llave para:
 * - Buscar la inscripción en la base de datos
 * - Generar la ruta (key) del archivo en S3: {id}/resumen_inscripcion.txt
 */
public interface InscripcionS3Service {

    // Genera el resumen como byte[] a partir de los datos de la inscripción en la BD
    // No sube nada a S3, solo crea el contenido del archivo en memoria
    // Se usa para descargar el resumen sin guardarlo en S3 (descarga directa)
    byte[] generarResumen(Long inscripcionId);

    // Genera el resumen Y lo sube a S3 en la ruta {id}/resumen_inscripcion.txt
    // Combina generarResumen + putObject de S3
    void subirResumen(Long inscripcionId);

    // Descarga el resumen que ya está almacenado en S3
    // Retorna el archivo como byte[] para que el controlador lo envíe al cliente
    byte[] descargarResumen(Long inscripcionId);

    // Modifica la inscripción en la BD y regenera el resumen en S3
    // Recibe el objeto Inscripcion con los datos nuevos (estudiante, cursos, total)
    // 1. Actualiza la inscripción en la BD
    // 2. Regenera el resumen con los datos actualizados
    // 3. Sube el nuevo resumen a S3 sobreescribiendo el anterior
    void modificarResumen(Long inscripcionId, Inscripcion inscripcion);

    // Elimina el resumen de S3
    // No elimina la inscripción de la BD, solo el archivo en S3
    void borrarResumen(Long inscripcionId);

}
