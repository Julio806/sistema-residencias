package com.tecnm.residencias.repository;

import com.tecnm.residencias.entity.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PeriodoRepository extends JpaRepository<PeriodoAcademico, Long> {
    // Para obtener una lista de periodos activos (ej. para mostrar en dropdowns)
    List<PeriodoAcademico> findAllByActivoTrue();

    Optional<Object> findByActivoTrue();
    List<PeriodoAcademico> findAllByActivoFalse();

}