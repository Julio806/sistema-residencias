package com.tecnm.residencias.dto;

public class CarreraCantidadDTO {
    private String carrera;
    private Long cantidad;

    public CarreraCantidadDTO(String carrera, Long cantidad) {
        this.carrera = carrera;
        this.cantidad = cantidad;
    }

    public String getCarrera() {
        return carrera;
    }

    public Long getCantidad() {
        return cantidad;
    }
}
