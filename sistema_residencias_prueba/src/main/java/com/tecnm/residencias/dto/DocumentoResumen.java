package com.tecnm.residencias.dto;

import java.time.LocalDateTime;

public class DocumentoResumen {

    private Long id;
    private String nombre;
    private String carrera;
    private String tipo;
    private LocalDateTime fechaSubida;
    private String estado;
    private String badgeClass;

    // Constructor para búsqueda por alumno (sin tipo ni carrera)
    public DocumentoResumen(Long id, String nombre, LocalDateTime fechaSubida, Boolean aceptado) {
        this.id = id;
        this.nombre = nombre;
        this.fechaSubida = fechaSubida;
        this.estado = (aceptado == null) ? "Pendiente" : aceptado ? "Aceptado" : "Rechazado";
        this.badgeClass = (aceptado == null) ? "bg-warning text-dark" : aceptado ? "bg-success" : "bg-danger";
    }

    // Constructor para búsqueda por periodo (incluye tipo y carrera)
    public DocumentoResumen(Long id, String nombre, String carrera, String tipo, LocalDateTime fechaSubida, Boolean aceptado) {
        this.id = id;
        this.nombre = nombre;
        this.carrera = carrera;
        this.tipo = tipo;
        this.fechaSubida = fechaSubida;
        this.estado = (aceptado == null) ? "Pendiente" : aceptado ? "Aceptado" : "Rechazado";
        this.badgeClass = (aceptado == null) ? "bg-warning text-dark" : aceptado ? "bg-success" : "bg-danger";
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCarrera() {
        return carrera;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public String getEstado() {
        return estado;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
