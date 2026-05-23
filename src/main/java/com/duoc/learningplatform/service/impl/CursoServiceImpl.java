package com.duoc.learningplatform.service.impl;

import com.duoc.learningplatform.model.Curso;
import com.duoc.learningplatform.model.Rol;
import com.duoc.learningplatform.model.Usuario;
import com.duoc.learningplatform.repository.CursoRepository;
import com.duoc.learningplatform.repository.UsuarioRepository;
import com.duoc.learningplatform.service.contrato.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CursoServiceImpl(CursoRepository cursoRepository, UsuarioRepository usuarioRepository) {
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Curso> findAll() {
        return cursoRepository.findAll();
    }

    @Override
    public Optional<Curso> findById(Long id) {
        return cursoRepository.findById(id);
    }

    @Override
    public Curso save(Curso curso) {
        Usuario profesor = usuarioRepository.findById(curso.getProfesor().getId())
                .orElseThrow(()->new RuntimeException("Profesor no encontrado"));
        if(profesor.getRol() == Rol.PROFESOR){
            curso.getProfesor().setNombre(profesor.getNombre());
            curso.getProfesor().setCorreo(profesor.getCorreo());
            curso.getProfesor().setRol(profesor.getRol());
            return cursoRepository.save(curso);
        }
        return null;
    }

    @Override
    public Optional<Curso> update(Long id, Curso curso) {
        //Existe el curso
        if (!cursoRepository.existsById(id)) {
            throw new RuntimeException("Curso no encontrado");
        }

        //Rescatamos al profesor
        Usuario profesor = usuarioRepository.findById(curso.getProfesor().getId())
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        //Si el profesor cumple con el ROL
        if (profesor.getRol() != Rol.PROFESOR) {
            throw new RuntimeException("El usuario no es un profesor");
        }

        return cursoRepository.findById(id).map(c -> {
            c.setNombre(curso.getNombre());
            c.setDescripcion(curso.getDescripcion());
            c.setProfesor(curso.getProfesor());
            c.setDuracion(curso.getDuracion());
            c.setValor(curso.getValor());
            return cursoRepository.save(c);
        });
    }

    @Override
    public Boolean delete(Long id) {
        if(cursoRepository.existsById(id)){
            cursoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
