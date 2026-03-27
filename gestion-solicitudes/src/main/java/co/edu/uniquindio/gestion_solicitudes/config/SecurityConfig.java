package co.edu.uniquindio.gestion_solicitudes.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.SecurityMarker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("""
                        {
                          "status": 401,
                          "error": "Unauthorized",
                          "mensaje": "Token JWT ausente o inválido.",
                          "path": "%s"
                        }
                        """.formatted(request.getRequestURI()));
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/solicitudes").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/solicitudes/*/clasificar").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/solicitudes/*/priorizar").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/solicitudes/*/iniciar-atencion").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/solicitudes/*/marcar-atendida").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/solicitudes/*/cerrar").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/solicitudes/*/cancelar").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/solicitudes/*/responsable").hasRole("ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.GET, "/solicitudes/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/historial/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
