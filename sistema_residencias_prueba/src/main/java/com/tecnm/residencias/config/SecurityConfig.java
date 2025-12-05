package com.tecnm.residencias.config;

import com.tecnm.residencias.service.UsuarioDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 🟢 Rutas públicas
                        .requestMatchers("/", "/dashboard", "/seleccionar-periodo", "/login", "/registro",
                                "/forgot-password", "/reset-password/**", "/css/**", "/images/**", "/js/**").permitAll()

                        // 👁 Vista previa de documentos
                        .requestMatchers("/documentos/ver/**").permitAll()

                        // 🔐 Rutas restringidas por rol
                        .requestMatchers("/mi_perfil/**").hasRole("ALUMNO")
                        .requestMatchers("/panel/**,/nuevo-periodo/**").hasRole("DIVISION")
                        .requestMatchers("/escolares/**").hasRole("ESCOLARES")
                        .requestMatchers("/vinculacion/**").hasRole("VINCULACION")
                        .requestMatchers("/ingenierias/**").hasRole("INGENIERIAS")

                        // 📄 Documentos y plantillas
                        .requestMatchers("/documentos/**").authenticated()
                        .requestMatchers("/plantillas/**").authenticated()

                        // ⚙️ Administración de periodos (solo ADMIN)
                        .requestMatchers("/crear-periodo", "/cambiarEstado").hasRole("DIVISION")

                        // Cualquier otra ruta requiere login
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
