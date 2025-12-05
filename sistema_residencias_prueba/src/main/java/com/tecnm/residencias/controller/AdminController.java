
package com.tecnm.residencias.controller;

import com.tecnm.residencias.entity.Alumno;
import com.tecnm.residencias.repository.AlumnoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AlumnoRepository alumnoRepository;

    @PreAuthorize("hasAnyRole('DIVISION', 'ESCOLARES', 'VINCULACION', 'INGENIERIAS')")
    @GetMapping("/alumnos")
    public String verListaAlumnos(@RequestParam(name = "carrera", required = false) String carrera,
                                  HttpSession session, Model model) {

        Long periodoId = (Long) session.getAttribute("periodoActivo");
        List<Alumno> alumnos;

        if (periodoId == null) {
            model.addAttribute("error", "No hay periodo activo seleccionado.");
            return "redirect:/dashboard";
        }

        if (carrera != null && !carrera.isEmpty()) {
            alumnos = alumnoRepository.findByCarreraIgnoreCaseAndPeriodoId(carrera, periodoId);
        } else {
            alumnos = alumnoRepository.findByPeriodoId(periodoId); // ✅ SOLO alumnos del periodo activo
        }

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("carrera", carrera);
        return "admin_alumnos";
    }

}
