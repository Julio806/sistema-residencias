package com.tecnm.residencias.controller;

import com.tecnm.residencias.entity.PeriodoAcademico;
import com.tecnm.residencias.repository.PeriodoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {
    private PeriodoRepository periodoRepository;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Authentication auth, Model model, HttpSession session) {
        List<PeriodoAcademico> periodos = periodoRepository.findAll();
        model.addAttribute("periodos", periodos);

        // Si necesitas usar el usuario autenticado también
        model.addAttribute("usuario", auth.getName());

        return "dashboard";
    }

}
