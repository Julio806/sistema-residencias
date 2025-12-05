package com.tecnm.residencias.dto;

import java.time.LocalDateTime;

public class DocumentoGeneradoResumen {

    private Long id;
    private String tipo;
    private String carrera;
    private String nombreArchivo;
    private LocalDateTime fecha;
    private String estado;
    private String badgeClass;

    public DocumentoGeneradoResumen(Long id, String tipo, String carrera, String nombreArchivo, LocalDateTime fecha, Boolean aceptado) {
        this.id = id;
        this.tipo = tipo;
        this.carrera = carrera;
        this.nombreArchivo = nombreArchivo;
        this.fecha = fecha;
        this.estado = (aceptado == null) ? "Pendiente" : aceptado ? "Firmado" : "Rechazado";
        this.badgeClass = (aceptado == null) ? "bg-warning text-dark" : aceptado ? "bg-success" : "bg-danger";
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCarrera() {
        return carrera;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
