package com.duoc.learningplatform.service.contrato;

import com.duoc.learningplatform.model.Inscripcion;

import java.util.List;

public interface InscripcionService {

    List<Inscripcion> findByCursoId(Long id);
    Inscripcion save(Inscripcion inscripcion);
    Boolean delete(Long id);
    Inscripcion findById(Long id);
}
