package com.duoc.learningplatform.controller;

import com.duoc.learningplatform.model.Inscripcion;
import com.duoc.learningplatform.service.contrato.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learningplatform/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    @Autowired
    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> buscarInscripcionPorId(@PathVariable Long id){
        try{
            return ResponseEntity.ok(inscripcionService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> guardarInscripcion(@RequestBody Inscripcion inscripcion){
        try{
            return ResponseEntity.status(201).body(inscripcionService.save(inscripcion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarInscripcion(@PathVariable Long id){
        try{
            return ResponseEntity.ok(inscripcionService.delete(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarInscripcion(@PathVariable Long id, @RequestBody Inscripcion inscripcion) {
        try {
            return ResponseEntity.ok(inscripcionService.update(id, inscripcion));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}