package com.tecnm.residencias.service;

import com.tecnm.residencias.entity.PeriodoAcademico;
import com.tecnm.residencias.repository.PeriodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeriodoService {
    @Autowired
    private PeriodoRepository periodoRepo;
    // Listar todos, incluyendo activos e inactivos
    public List<PeriodoAcademico> listarTodos() {
        return periodoRepo.findAll();
    }
    // Usar esto siempre para vistas, lógica, controladores, etc.
    public List<PeriodoAcademico> listarActivos() {
        return periodoRepo.findAllByActivoTrue();
    }

    // Puedes usar esto si necesitas obtener uno en particular por ID
    public PeriodoAcademico obtenerActivo() {
        return (PeriodoAcademico) periodoRepo.findByActivoTrue()
                .orElseThrow(() -> new IllegalStateException("No hay periodo activo"));
    }
    // Cambiar estado (activar/inactivar)
    public void cambiarEstado(Long id, boolean activo) {
        PeriodoAcademico periodo = periodoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Periodo no encontrado"));
        periodo.setActivo(activo);
        periodoRepo.save(periodo);
    }
}
