package com.tecnm.residencias.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] datos;

    private Boolean aceptado;

    private LocalDateTime fechaSubida;

    private String tipo;      // NUEVO: tipo de documento (Solicitud, Carta, etc.)
    private String carrera;   // NUEVO: carrera del documento

    @ManyToOne
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "periodo_id")
    private PeriodoAcademico periodo;

    @PrePersist
    public void setFechaSubida() {
        this.fechaSubida = LocalDateTime.now();
    }

    // Getters y Setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public byte[] getDatos() { return datos; }

    public void setDatos(byte[] datos) { this.datos = datos; }

    public Boolean getAceptado() { return aceptado; }

    public void setAceptado(Boolean aceptado) { this.aceptado = aceptado; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }

    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }

    public String getTipo() { return tipo; }

    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCarrera() { return carrera; }

    public void setCarrera(String carrera) { this.carrera = carrera; }

    public Alumno getAlumno() { return alumno; }

    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public PeriodoAcademico getPeriodo() { return periodo; }

    public void setPeriodo(PeriodoAcademico periodo) { this.periodo = periodo; }
}
