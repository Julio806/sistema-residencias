package com.tecnm.residencias.repository;// NotificacionRepository.java
import com.tecnm.residencias.entity.Alumno;
import com.tecnm.residencias.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByAlumnoAndLeidaFalseOrderByFechaDesc(Alumno alumno);
}
