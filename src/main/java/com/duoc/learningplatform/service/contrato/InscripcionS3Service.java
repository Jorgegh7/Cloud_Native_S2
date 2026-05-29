package com.duoc.learningplatform.service.contrato;

import com.duoc.learningplatform.model.Inscripcion;
import org.springframework.web.multipart.MultipartFile;

public interface InscripcionS3Service {

    byte[] generarResumen(Long inscripcionId);
    void subirResumen(Long inscripcionId);
    byte[] descargarResumen(Long inscripcionId);
    void modificarResumen(Long inscripcionId, Inscripcion inscripcion);
    void borrarResumen(Long inscripcionId);

}
