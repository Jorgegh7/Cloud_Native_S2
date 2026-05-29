package com.duoc.learningplatform.service.impl;

import com.duoc.learningplatform.model.Curso;
import com.duoc.learningplatform.model.Inscripcion;
import com.duoc.learningplatform.model.Rol;
import com.duoc.learningplatform.model.Usuario;
import com.duoc.learningplatform.repository.CursoRepository;
import com.duoc.learningplatform.repository.InscripcionRepository;
import com.duoc.learningplatform.repository.UsuarioRepository;
import com.duoc.learningplatform.service.contrato.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public InscripcionServiceImpl(InscripcionRepository inscripcionRepository, CursoRepository cursoRepository, UsuarioRepository usuarioRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Inscripcion save(Inscripcion inscripcion) {
        //Validar Usuario
        Usuario estudiante = usuarioRepository.findById(inscripcion.getEstudiante().getId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        if (estudiante.getRol() != Rol.ESTUDIANTE) {
            throw new RuntimeException("El usuario no es un estudiante");
        }

        List<Curso> cursosValidados = new ArrayList<>();
        Long total = 0L;

        //Iteramos la lista de cursos y validamos
        for (Curso curso : inscripcion.getCursos()) {
            Curso cursoDb = cursoRepository.findById(curso.getId())
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado con id: " + curso.getId()));
            cursosValidados.add(cursoDb);
            total += cursoDb.getValor();
        }

        //Guardan los valores validados
        inscripcion.setEstudiante(estudiante);
        inscripcion.setCursos(cursosValidados);
        inscripcion.setTotalPagar(total);
        return inscripcionRepository.save(inscripcion);
    }

    public Boolean delete(Long id) {
        if(inscripcionRepository.existsById(id)){
            inscripcionRepository.deleteById(id);
            return true;
        }
        throw new RuntimeException("Inscripción no encontrada");
    }

    @Override
    public Inscripcion findById(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscripcion no encontrada"));
    }

    @Override
    public Inscripcion update(Long id, Inscripcion inscripcion) {
        Inscripcion existente = inscripcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        Usuario estudiante = usuarioRepository.findById(inscripcion.getEstudiante().getId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        existente.setEstudiante(estudiante);
        existente.setCursos(inscripcion.getCursos());
        existente.setTotalPagar(inscripcion.getTotalPagar());
        return inscripcionRepository.save(existente);
    }
}
