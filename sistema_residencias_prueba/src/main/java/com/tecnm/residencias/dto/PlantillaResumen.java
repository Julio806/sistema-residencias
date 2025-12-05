package com.tecnm.residencias.dto;

import java.time.LocalDateTime;

public class PlantillaResumen {
    private Long id;
    private String tipo;
    private String nombreArchivo;
    private LocalDateTime fechaSubida;

    public PlantillaResumen(Long id, String tipo, String nombreArchivo, LocalDateTime fechaSubida) {
        this.id = id;
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.fechaSubida = fechaSubida;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }
}
