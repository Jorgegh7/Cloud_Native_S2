package com.duoc.learningplatform.controller;

import com.duoc.learningplatform.model.Curso;
import com.duoc.learningplatform.service.contrato.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/learningplatform/cursos")
public class CursoController {

    private final CursoService cursoService;

    @Autowired
    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }


    @GetMapping
    public ResponseEntity<List<Curso>> listaCursos(){
        return ResponseEntity.ok(cursoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Curso>> buscarPorId(@PathVariable Long id){
        return ResponseEntity.ok(cursoService.findById(id));
    }


    @PostMapping
    public ResponseEntity<?> guardarCurso(@RequestBody Curso curso){
        try {
            Curso cursoGuardado = cursoService.save(curso);
            if (cursoGuardado != null) {
                return ResponseEntity.status(201).body(cursoGuardado);
            }
            return ResponseEntity.badRequest().body("El usuario no es un profesor");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Optional<Curso>> actualizarCurso(@PathVariable Long id, @RequestBody Curso curso){
        return ResponseEntity.ok(cursoService.update(id,curso));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> eliminarCurso(@PathVariable Long id){
        return ResponseEntity.ok(cursoService.delete(id));

    }

}