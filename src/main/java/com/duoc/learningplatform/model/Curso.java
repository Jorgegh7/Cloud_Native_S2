package com.duoc.learningplatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cursos")
@NoArgsConstructor
@Getter
@Setter
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Debes ingresar el nombre del curso")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotBlank
    @Column(nullable = false, length = 250)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    private Long valor;

    @NotNull
    @Column(nullable = false)
    @Min(1)
    private int duracion;

    public Curso(String nombre, String descripcion, Usuario profesor) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.profesor = profesor;
    }
}