package com.tecnm.residencias.config;

import com.tecnm.residencias.entity.Alumno;
import com.tecnm.residencias.entity.Usuario;
import com.tecnm.residencias.repository.AlumnoRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PeriodoAccessFilter implements Filter {

    @Autowired
    private AlumnoRepository alumnoRepo;

    @Override
    public void doFilter(jakarta.servlet.ServletRequest servletRequest,
                         jakarta.servlet.ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (session != null && auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();

            if (principal instanceof Usuario usuario && usuario.getRol() != null) {

                // Solo validamos acceso si el usuario es alumno
                if ("ALUMNO".equalsIgnoreCase(usuario.getRol().getNombre())) {
                    Object periodoObj = session.getAttribute("periodoActivo");
                    Long periodoActivo = null;

                    // Convertimos a Long si es posible
                    if (periodoObj instanceof Long) {
                        periodoActivo = (Long) periodoObj;
                    } else if (periodoObj instanceof String) {
                        try {
                            periodoActivo = Long.parseLong((String) periodoObj);
                        } catch (NumberFormatException e) {
                            // Ignorar
                        }
                    }

                    // Si no se pudo obtener un periodo válido
                    if (periodoActivo == null) {
                        session.invalidate();
                        response.sendRedirect("/seleccionar-periodo?error=periodo-invalido");
                        return;
                    }

                    // Validamos que el alumno pertenezca al periodo activo
                    Alumno alumno = alumnoRepo.findByUsuario(usuario);
                    if (alumno == null || alumno.getPeriodo() == null ||
                            !alumno.getPeriodo().getId().equals(periodoActivo)) {
                        session.invalidate();
                        response.sendRedirect("/seleccionar-periodo?error=acceso-denegado");
                        return;
                    }
                }
            }
        }

        // Si no aplica la validación o pasa todo correctamente
        chain.doFilter(servletRequest, servletResponse);
    }
}
