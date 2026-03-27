package co.edu.uniquindio.gestion_solicitudes.config;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws  Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    // Endpoints publicos
                        .requestMatchers("/api/auth/").permitAll()
                    // Registro de solicitudes: cualquier rol autenticado
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes").authenticated()

                    // Clasificar, priorizar, gestionar estados: solo DOCENTE o ADMINISTRATIVO
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/clasificar").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/priorizar").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/iniciar-atencion").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/marcar-atendida").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/cerrar").hasAnyRole("DOCENTE", "ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/cancelar").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/responsable").hasRole("ADMINISTRATIVO")

                    //Consultas: cualquier autenticado
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/historial/**").authenticated()
                    // Todo lo demás requiere autenticación
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
