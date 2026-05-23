package com.duoc.learningplatform.service.contrato;

import com.duoc.learningplatform.model.Curso;

import java.util.List;
import java.util.Optional;

public interface CursoService {

    List<Curso> findAll();
    Optional<Curso> findById(Long id);
    Curso save(Curso curso);
    Optional<Curso> update(Long id, Curso curso);
    Boolean delete(Long id);

}
