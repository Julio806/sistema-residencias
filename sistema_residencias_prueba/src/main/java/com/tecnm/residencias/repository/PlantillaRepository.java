package com.tecnm.residencias.repository;

import com.tecnm.residencias.entity.Plantilla;
import com.tecnm.residencias.dto.PlantillaResumen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface PlantillaRepository extends JpaRepository<Plantilla, Long> {

    // ✅ Para backend: obtiene la entidad completa con LOB incluido
    Optional<Plantilla> findByTipoIgnoreCase(String tipo);

    // ✅ Para la vista: obtiene solo resumenes sin cargar el campo @Lob
    @Query("SELECT new com.tecnm.residencias.dto.PlantillaResumen(p.id, p.tipo, p.nombreArchivo, p.fechaSubida) " +
            "FROM Plantilla p ORDER BY p.tipo")
    List<PlantillaResumen> findResumenes();

    // ✅ Para orden alfabético
    List<Plantilla> findAllByOrderByTipoAsc();
}
