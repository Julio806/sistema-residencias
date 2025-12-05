package com.tecnm.residencias.repository;

import com.tecnm.residencias.dto.CarreraCantidadDTO;
import com.tecnm.residencias.entity.Alumno;
import com.tecnm.residencias.entity.PeriodoAcademico;
import com.tecnm.residencias.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    // Buscar alumnos por filtro (sin considerar periodo)
    @Query("SELECT a FROM Alumno a WHERE " +
            "LOWER(CONCAT(a.nombres, ' ', a.apellidos)) LIKE %:filtro% OR " +
            "LOWER(a.numeroControl) LIKE %:filtro% OR " +
            "LOWER(a.carrera) LIKE %:filtro%")
    List<Alumno> findByFiltro(@Param("filtro") String filtro);

    // Buscar alumnos por filtro dentro de un periodo específico
    @Query("SELECT a FROM Alumno a WHERE " +
            "(LOWER(a.nombres) LIKE %:filtro% OR " +
            "LOWER(a.apellidos) LIKE %:filtro% OR " +
            "LOWER(a.numeroControl) LIKE %:filtro%) AND " +
            "a.periodo.id = :periodoId")
    List<Alumno> findByFiltroYPeriodo(@Param("filtro") String filtro, @Param("periodoId") Long periodoId);

    // Buscar todos los alumnos de un periodo
    List<Alumno> findByPeriodoId(Long periodoId);

    // Buscar alumnos por carrera ignorando mayúsculas
    List<Alumno> findByCarreraIgnoreCase(String carrera);

    // Buscar alumnos por carrera y periodo
    List<Alumno> findByCarreraIgnoreCaseAndPeriodoId(String carrera, Long periodoId);

    // Buscar alumno por relación con usuario
    Alumno findByUsuario(Usuario usuario);

    // Distribución por carrera (general)
    @Query("SELECT new com.tecnm.residencias.dto.CarreraCantidadDTO(a.carrera, COUNT(a)) " +
            "FROM Alumno a GROUP BY a.carrera")
    List<CarreraCantidadDTO> contarPorCarrera();

    // Distribución por carrera dentro de un periodo
    @Query("SELECT new com.tecnm.residencias.dto.CarreraCantidadDTO(a.carrera, COUNT(a)) " +
            "FROM Alumno a WHERE a.periodo.id = :periodoId GROUP BY a.carrera")
    List<CarreraCantidadDTO> contarPorCarreraYPeriodo(@Param("periodoId") Long periodoId);

    List<Alumno> findByCarreraAndPeriodo_Id(String carrera, Long id);
    List<Alumno> findByCarreraIgnoreCaseAndPeriodoActivo(String carrera, boolean activo);

    List<Alumno> findByCarreraIgnoreCaseAndPeriodo(String carrera, PeriodoAcademico periodo);
    @Query("SELECT a FROM Alumno a WHERE " +
            "LOWER(a.carrera) = LOWER(:carrera) AND " +
            "(LOWER(CONCAT(a.nombres, ' ', a.apellidos)) LIKE %:filtro% OR " +
            "LOWER(a.numeroControl) LIKE %:filtro%) AND " +
            "a.periodo.id = :periodoId")
    List<Alumno> findByCarreraAndFiltroYPeriodo(@Param("carrera") String carrera,
                                                @Param("filtro") String filtro,
                                                @Param("periodoId") Long periodoId);

}
