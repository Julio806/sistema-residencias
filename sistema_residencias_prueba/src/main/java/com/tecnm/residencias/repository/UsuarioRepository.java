package com.tecnm.residencias.repository;

import com.tecnm.residencias.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByCorreoAlternativo(String correoAlternativo);
    boolean existsByCorreo(String correo);

}