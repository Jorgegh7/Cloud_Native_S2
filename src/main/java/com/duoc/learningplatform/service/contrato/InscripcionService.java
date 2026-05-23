package com.duoc.learningplatform.service.contrato;

import com.duoc.learningplatform.model.Inscripcion;

public interface InscripcionService {

    Inscripcion save(Inscripcion inscripcion);
    Boolean delete(Long id);
    Inscripcion findById(Long id);
}
