package com.duoc.learningplatform.controller;

import com.duoc.learningplatform.model.Inscripcion;
import com.duoc.learningplatform.service.contrato.InscripcionS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST que expone los endpoints para gestionar los resúmenes
 * de inscripción en AWS S3.
 *
 * Este es el controlador principal de la Semana 2. Conecta las peticiones
 * HTTP del cliente (Postman) con el servicio InscripcionS3Service.
 *
 * Ruta base: /inscripciones
 * Todos los endpoints siguen el patrón: /inscripciones/{id}/resumen/{accion}
 *
 * Endpoints disponibles:
 *   GET    /inscripciones/{id}/resumen/generar    → genera y descarga sin tocar S3
 *   POST   /inscripciones/{id}/resumen/subir      → genera desde BD y sube a S3
 *   GET    /inscripciones/{id}/resumen/descargar   → descarga el archivo desde S3
 *   PUT    /inscripciones/{id}/resumen/modificar   → actualiza BD + regenera en S3
 *   DELETE /inscripciones/{id}/resumen/borrar      → elimina el archivo de S3
 *
 * Nota: todos los endpoints reciben el ID de la inscripción como @PathVariable.
 * Este ID se usa tanto para buscar la inscripción en la BD como para
 * construir la ruta del archivo en S3 ({id}/resumen_inscripcion.txt).
 */

@RestController
@RequestMapping("/inscripciones")
@RequiredArgsConstructor
public class InscripcionS3Controller {

    private final InscripcionS3Service inscripcionS3Service;

    /**
     * Genera el resumen desde la BD y lo devuelve como archivo descargable.
     * GET /inscripciones/{id}/resumen/generar
     *
     * Este endpoint NO interactúa con S3. Solo:
     * 1. Busca la inscripción en la BD
     * 2. Genera el texto del resumen
     * 3. Lo devuelve como archivo al cliente
     *
     * Útil para previsualizar el resumen antes de subirlo a S3
     * o para descargar una copia sin depender de S3.
     *
     * CONTENT_DISPOSITION con "attachment": le dice a Postman/navegador que
     * la respuesta es un archivo descargable, no contenido para mostrar.
     * El filename define el nombre con que se guarda: "resumen_inscripcion_1.txt"
     *
     * MediaType.TEXT_PLAIN: indica que el contenido es texto plano,
     * así Postman puede mostrarlo directamente en pantalla.
     */
    @GetMapping("/{id}/resumen/generar")
    public ResponseEntity<?> generarResumen(@PathVariable Long id) {
        try {
            byte[] resumen = inscripcionS3Service.generarResumen(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resumen_inscripcion_" + id + ".txt")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resumen);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Genera el resumen desde la BD y lo sube a AWS S3.
     * POST /inscripciones/{id}/resumen/subir
     *
     * No requiere body en la petición. El resumen se genera automáticamente
     * desde los datos de la inscripción que están en la BD.
     *
     * El archivo se guarda en S3 en la ruta: {id}/resumen_inscripcion.txt
     * Ejemplo: inscripción 1 → "1/resumen_inscripcion.txt" en mslpbucket
     *
     * ResponseEntity<String>: devuelve un mensaje de confirmación como texto.
     */
    @PostMapping("/{id}/resumen/subir")
    public ResponseEntity<?> subirResumen(@PathVariable Long id) {
        try {
            inscripcionS3Service.subirResumen(id);
            return ResponseEntity.ok("Resumen de inscripción #" + id + " subido a S3");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Descarga el resumen que ya está almacenado en S3.
     * GET /inscripciones/{id}/resumen/descargar
     *
     * Diferencia con /generar:
     *   - /generar: crea el resumen desde la BD (S3 no participa)
     *   - /descargar: baja el archivo que ya existe en S3
     *
     * Si el archivo no existe en S3 (no se ha subido), dará error.
     *
     * MediaType.APPLICATION_OCTET_STREAM: tipo genérico para archivos binarios.
     * Se usa aquí porque el archivo viene de S3 y podría ser cualquier tipo.
     * A diferencia de /generar que usa TEXT_PLAIN porque sabemos que es texto.
     */
    @GetMapping("/{id}/resumen/descargar")
    public ResponseEntity<?> descargarResumen(@PathVariable Long id) {
        try {
            byte[] resumen = inscripcionS3Service.descargarResumen(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resumen_inscripcion_" + id + ".txt")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resumen);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Modifica la inscripción en la BD y regenera el resumen en S3.
     * PUT /inscripciones/{id}/resumen/modificar
     * Body: JSON con los datos nuevos de la inscripción
     *
     * Ejemplo body:
     * {
     *   "estudiante": { "id": 1 },
     *   "cursos": [{ "id": 2 }],
     *   "totalPagar": 75000
     * }
     *
     * @RequestBody: Spring convierte el JSON del body en un objeto Inscripcion.
     * A diferencia de @RequestParam que extrae datos de la URL,
     * @RequestBody extrae datos del cuerpo de la petición.
     *
     * Internamente hace dos cosas en orden:
     * 1. Actualiza la inscripción en la BD con los datos nuevos
     * 2. Regenera el resumen con los datos actualizados y lo sube a S3
     *
     * Así se mantiene la consistencia entre la BD y S3 en una sola llamada.
     */
    @PutMapping("/{id}/resumen/modificar")
    public ResponseEntity<String> modificarResumen(@PathVariable Long id, @RequestBody Inscripcion inscripcion) {
        inscripcionS3Service.modificarResumen(id, inscripcion);
        return ResponseEntity.ok("Inscripción #" + id + " actualizada y resumen regenerado en S3");
    }

    /**
     * Elimina el resumen de S3.
     * DELETE /inscripciones/{id}/resumen/borrar
     *
     * Solo borra el archivo en S3, NO elimina la inscripción de la BD.
     * Si se necesita el resumen de nuevo, se puede volver a generar con /subir.
     *
     * Si el archivo no existe en S3, no da error (S3 es independiente en delete).
     */
    @DeleteMapping("/{id}/resumen/borrar")
    public ResponseEntity<String> borrarResumen(@PathVariable Long id) {
        inscripcionS3Service.borrarResumen(id);
        return ResponseEntity.ok("Resumen de inscripción #" + id + " borrado de S3");
    }
}