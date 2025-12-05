package com.tecnm.residencias.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "plantillas")
public class Plantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    private String nombreArchivo;

    @Lob
    private byte[] archivo;

    private LocalDateTime fechaSubida;

    @PrePersist
    public void setFechaSubida() {
        this.fechaSubida = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }

    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNombreArchivo() { return nombreArchivo; }

    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public byte[] getArchivo() { return archivo; }

    public void setArchivo(byte[] archivo) { this.archivo = archivo; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }

    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
}
