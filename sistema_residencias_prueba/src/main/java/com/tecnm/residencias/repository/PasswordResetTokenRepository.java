package com.tecnm.residencias.repository;

import com.tecnm.residencias.entity.PasswordResetToken;
import com.tecnm.residencias.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    // Buscar token existente por usuario
    Optional<PasswordResetToken> findByUsuario(Usuario usuario);

    // Buscar token por valor del token (para reset)
}
