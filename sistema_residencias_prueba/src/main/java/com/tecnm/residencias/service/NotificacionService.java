package com.tecnm.residencias.service;

import com.tecnm.residencias.entity.Alumno;
import com.tecnm.residencias.entity.Notificacion;
import com.tecnm.residencias.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository repo;

    // Crear nueva notificación
    public void crear(Alumno alumno, String mensaje) {
        Notificacion n = new Notificacion();
        n.setAlumno(alumno);
        n.setMensaje(mensaje);
        n.setFecha(LocalDateTime.now());
        n.setLeida(false); // marcar como no leída por defecto
        repo.save(n);
    }

    // Obtener notificaciones no leídas del alumno
    public List<Notificacion> obtenerNoLeidas(Alumno alumno) {
        return repo.findByAlumnoAndLeidaFalseOrderByFechaDesc(alumno);
    }

    // Marcar todas las notificaciones como leídas
    public void marcarTodasComoLeidas(Alumno alumno) {
        List<Notificacion> pendientes = repo.findByAlumnoAndLeidaFalseOrderByFechaDesc(alumno);
        for (Notificacion n : pendientes) {
            n.setLeida(true);
        }
        repo.saveAll(pendientes);
    }
}
