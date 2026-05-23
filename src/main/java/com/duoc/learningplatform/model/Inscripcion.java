package com.duoc.learningplatform.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "inscripciones")
@Getter
@Setter
@NoArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    @ManyToMany
    @JoinTable(
            name = "inscripcion_cursos",
            joinColumns = @JoinColumn(name = "inscripcion_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    private List<Curso> cursos = new ArrayList<>();

    @Column(nullable = false)
    private Date fechaInscripcion;

    @Column(nullable = false)
    private Long totalPagar;

    @PrePersist
    public void prePersist() {
        this.fechaInscripcion = new Date();
    }
}