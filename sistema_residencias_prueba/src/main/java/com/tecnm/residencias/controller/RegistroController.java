package com.tecnm.residencias.controller;

import com.tecnm.residencias.entity.Alumno;
import com.tecnm.residencias.entity.Rol;
import com.tecnm.residencias.entity.Usuario;
import com.tecnm.residencias.repository.AlumnoRepository;
import com.tecnm.residencias.repository.PeriodoRepository;
import com.tecnm.residencias.repository.RolRepository;
import com.tecnm.residencias.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistroController {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private AlumnoRepository alumnoRepo;
    @Autowired private PeriodoRepository periodoRepo;
    @Autowired private RolRepository rolRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("alumno", new Alumno());
        model.addAttribute("periodos", periodoRepo.findAll());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarAlumno(@ModelAttribute Alumno alumno,
                                  @RequestParam String correo,
                                  @RequestParam String clave,
                                  Model model) {

        // Validar dominio del correo
        if (!correo.endsWith("@fcomalapa.tecnm.mx")) {
            model.addAttribute("error", "Solo se permiten correos institucionales (@fcomalapa.tecnm.mx)");
            model.addAttribute("periodos", periodoRepo.findAll());
            return "registro";
        }

        // Validar si ya existe el correo
        if (usuarioRepo.existsByCorreo(correo)) {
            model.addAttribute("error", "Este correo ya está registrado");
            model.addAttribute("periodos", periodoRepo.findAll());
            return "registro";
        }

        // Buscar el rol ALUMNO
        Rol rolAlumno = rolRepo.findByNombre("ALUMNO")
                .orElseThrow(() -> new RuntimeException("Rol ALUMNO no encontrado"));

        // Crear el usuario
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setClave(passwordEncoder.encode(clave));
        usuario.setActivo(true);
        usuario.setRol(rolAlumno);

        usuarioRepo.save(usuario);

        // Asociar con el alumno
        alumno.setUsuario(usuario);
        alumnoRepo.save(alumno);

        return "redirect:/login?registroExitoso";
    }
}
