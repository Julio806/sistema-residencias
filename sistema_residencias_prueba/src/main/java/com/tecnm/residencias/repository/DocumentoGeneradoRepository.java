package com.tecnm.residencias.repository;

import com.tecnm.residencias.dto.DocumentoGeneradoResumen;
import com.tecnm.residencias.entity.DocumentoGenerado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentoGeneradoRepository extends JpaRepository<DocumentoGenerado, Long> {
    List<DocumentoGenerado> findByPeriodoId(Long periodoId);
    @Query("SELECT new com.tecnm.residencias.dto.DocumentoGeneradoResumen(" +
            "d.id, d.tipo, d.carrera, d.nombreArchivo, d.fecha, d.aceptado) " +
            "FROM DocumentoGenerado d WHERE d.periodo.id = :periodoId")
    List<DocumentoGeneradoResumen> findResumenByPeriodo(@Param("periodoId") Long periodoId);

}
