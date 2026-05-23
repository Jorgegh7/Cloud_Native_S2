package com.duoc.learningplatform.repository;

import com.duoc.learningplatform.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
