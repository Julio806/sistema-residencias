package com.tecnm.residencias.config;

import com.tecnm.residencias.entity.Alumno;
import com.tecnm.residencias.entity.Usuario;
import com.tecnm.residencias.repository.AlumnoRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AlumnoRepository alumnoRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        HttpSession session = request.getSession();

        Object principal = authentication.getPrincipal();

        if (roles.contains("ROLE_ALUMNO")) {
            Object rawPeriodo = session.getAttribute("periodoActivo");
            Long periodoActivoId = null;

            if (rawPeriodo instanceof Long) {
                periodoActivoId = (Long) rawPeriodo;
            } else {
                session.invalidate();
                response.sendRedirect("/seleccionar-periodo?error=periodo_invalido");
                return;
            }

            if (principal instanceof Usuario usuario &&
                    "ALUMNO".equals(usuario.getRol().getNombre())) {

                Alumno alumno = alumnoRepo.findByUsuario(usuario);

                if (alumno == null || alumno.getPeriodo() == null ||
                        !alumno.getPeriodo().getId().equals(periodoActivoId)) {

                    SecurityContextHolder.clearContext();
                    session.invalidate();
                    response.sendRedirect("/seleccionar-periodo?error=acceso");
                    return;
                }
            }

            response.sendRedirect("/mi_perfil");
            return;
        }

        // 🔓 Otros roles NO son validados por periodo
        if (roles.contains("ROLE_DIVISION")) {
            response.sendRedirect("/panel");
        } else if (roles.contains("ROLE_ESCOLARES")) {
            response.sendRedirect("/escolares/inicio");
        } else if (roles.contains("ROLE_VINCULACION")) {
            response.sendRedirect("/vinculacion/inicio");
        } else if (roles.contains("ROLE_INGENIERIAS")) {
            response.sendRedirect("/ingenierias");
        } else {
            response.sendRedirect("/login?error");
        }
    }

}
