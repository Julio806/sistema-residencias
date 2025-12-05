package com.tecnm.residencias.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiracion;

    @Column(nullable = false)
    private boolean usado;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true) // 1 token por usuario
    private Usuario usuario;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getExpiracion() { return expiracion; }
    public void setExpiracion(LocalDateTime expiracion) { this.expiracion = expiracion; }

    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }
}
