package com.tecnm.residencias.controller;

import com.tecnm.residencias.entity.Alumno;
import com.tecnm.residencias.repository.AlumnoRepository;
import com.tecnm.residencias.repository.PeriodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DepartamentoController {

    @Autowired
    private PeriodoRepository periodoRepo;

    @Autowired
    private AlumnoRepository alumnoRepo;

    @GetMapping("/division/inicio")
    public String inicioDivision(Model model) {
        List<Alumno> alumnos = alumnoRepo.findAll();

        long totalConProyecto = alumnos.stream()
                .filter(a -> a.getNombreProyecto() != null && !a.getNombreProyecto().isEmpty())
                .count();

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("alumno", new Alumno());
        model.addAttribute("periodos", periodoRepo.findAll());
        model.addAttribute("totalConProyecto", totalConProyecto);

        return "panel";
    }



    @GetMapping("/escolares/inicio")
    public String inicioEscolares() {
        return "en_construccion";
    }

    @GetMapping("/vinculacion/inicio")
    public String inicioVinculacion() {
        return "en_construccion";
    }

    @GetMapping("/ingenierias/inicio")
    public String inicioIngenierias() {
        return "en_construccion";
    }
}
