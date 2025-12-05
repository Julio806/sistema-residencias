package com.tecnm.residencias.entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "periodo_academico")
public class PeriodoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @DateTimeFormat(pattern = "yyyy-MM-dd") // 👈 ¡Agrega esto!
    @Temporal(TemporalType.DATE)
    private Date fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd") // 👈 ¡Y esto también!
    @Temporal(TemporalType.DATE)
    private Date fechaFin;

    @Column(nullable = false)
    private boolean activo;

    @OneToMany(mappedBy = "periodo")
    private List<Alumno> alumnos;

    // ✅ Constructor requerido para setPeriodo(new PeriodoAcademico(id));
    public PeriodoAcademico(Long id) {
        this.id = id;
    }

    // ✅ Constructor vacío requerido por JPA
    public PeriodoAcademico() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Alumno> getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(List<Alumno> alumnos) {
        this.alumnos = alumnos;
    }
}
