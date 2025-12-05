package com.tecnm.residencias.controller;

import com.tecnm.residencias.entity.PasswordResetToken;
import com.tecnm.residencias.entity.Usuario;
import com.tecnm.residencias.repository.PasswordResetTokenRepository;
import com.tecnm.residencias.repository.UsuarioRepository;
import com.tecnm.residencias.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class PasswordController {

    private final UsuarioRepository usuarioRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordController(UsuarioRepository usuarioRepo,
                              PasswordResetTokenRepository tokenRepo,
                              EmailService emailService,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // Mostrar formulario forgot password
    @GetMapping("/forgot-password")
    public String mostrarForgotPassword() {
        return "forgot_password";
    }

    // Procesar solicitud de recuperación
    @PostMapping("/forgot-password")
    public String procesarForgotPassword(@RequestParam String correo, RedirectAttributes redirectAttributes) {

        // Buscar usuario por correo institucional o alternativo
        Usuario usuario = usuarioRepo.findByCorreo(correo)
                .orElse(usuarioRepo.findByCorreoAlternativo(correo).orElse(null));

        // Mensaje genérico por seguridad
        redirectAttributes.addFlashAttribute("mensaje",
                "Si existe una cuenta con este correo, recibirás un enlace para restablecer la contraseña.");

        if (usuario != null) {

            // Evitar token duplicado: eliminar si ya existía
            tokenRepo.findByUsuario(usuario).ifPresent(tokenRepo::delete);

            // Crear nuevo token
            String token = UUID.randomUUID().toString();
            PasswordResetToken prt = new PasswordResetToken();
            prt.setUsuario(usuario);
            prt.setToken(token);
            prt.setExpiracion(LocalDateTime.now().plusMinutes(30));
            prt.setUsado(false);
            tokenRepo.save(prt);

            // Construir enlace para restablecer contraseña
            String link = "http://localhost:8080/reset-password?token=" + token;

            // Preparar mensaje
            String mensaje = "Hola,\n\nPara cambiar tu contraseña, haz clic en el siguiente enlace:\n"
                    + link + "\n\nSi no solicitaste esto, ignora este mensaje.";

            // Enviar a ambos correos (si alternativo no es nulo y es distinto)
            if (usuario.getCorreoAlternativo() != null && !usuario.getCorreoAlternativo().isEmpty()
                    && !usuario.getCorreoAlternativo().equalsIgnoreCase(usuario.getCorreo())) {
                String[] destinatarios = {usuario.getCorreo(), usuario.getCorreoAlternativo()};
                emailService.enviarCorreo(destinatarios, "Recuperación de contraseña", mensaje);
            } else {
                emailService.enviarCorreo(usuario.getCorreo(), "Recuperación de contraseña", mensaje);
            }
        }

        return "redirect:/forgot-password";
    }

    // Mostrar formulario reset password
    @GetMapping("/reset-password")
    public String mostrarResetPassword(@RequestParam String token, Model model, RedirectAttributes redirectAttributes) {
        PasswordResetToken prt = tokenRepo.findByToken(token).orElse(null);

        if (prt == null || prt.isUsado() || prt.getExpiracion().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Token inválido o expirado.");
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        return "reset_password";
    }

    // Procesar cambio de contraseña
    @PostMapping("/reset-password")
    public String procesarResetPassword(@RequestParam String token,
                                        @RequestParam String clave,
                                        @RequestParam String confirmarClave,
                                        RedirectAttributes redirectAttributes) {

        if (!clave.equals(confirmarClave)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/reset-password?token=" + token;
        }

        PasswordResetToken prt = tokenRepo.findByToken(token).orElse(null);

        if (prt == null || prt.isUsado() || prt.getExpiracion().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Token inválido o expirado.");
            return "redirect:/login";
        }

        Usuario usuario = prt.getUsuario();
        usuario.setClave(passwordEncoder.encode(clave));
        usuarioRepo.save(usuario);

        prt.setUsado(true);
        tokenRepo.save(prt);

        redirectAttributes.addFlashAttribute("mensaje", "Contraseña actualizada correctamente. Ahora puedes iniciar sesión.");
        return "redirect:/login";
    }
}
