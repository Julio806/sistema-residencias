package com.tecnm.residencias.repository;

import com.tecnm.residencias.dto.DocumentoResumen;
import com.tecnm.residencias.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    @Query("SELECT new com.tecnm.residencias.dto.DocumentoResumen(d.id, d.nombre, d.fechaSubida, d.aceptado) " +
            "FROM Documento d WHERE d.alumno.id = :alumnoId")
    List<DocumentoResumen> findResumenesByAlumnoId(Long alumnoId);

    List<Documento> findByAlumnoId(Long alumnoId);

    List<Documento> findByPeriodoId(Long periodoId);
    @Query("""
    SELECT new com.tecnm.residencias.dto.DocumentoResumen(
        d.id, d.nombre, d.alumno.carrera, d.tipo, d.fechaSubida, d.aceptado
    )
    FROM Documento d
    WHERE d.periodo.id = :periodoId
""")
    List<DocumentoResumen> findResumenesByPeriodoId(Long periodoId);
    List<Documento> findByPeriodoIdAndAlumnoIsNull(Long periodoId);


}
