package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.dto.request.LoginRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.RegistroRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.AuthResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.UsuarioRepository;
import co.edu.uniquindio.gestion_solicitudes.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse registro(RegistroRequest request){
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()){
            throw new IllegalStateException("Ya existe un usuario con el email " + request.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        String token = jwtUtil.generarToken(
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getId()
        );

        return AuthResponse.builder()
                .token(token)
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }

    public AuthResponse login(LoginRequest request){
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Credenciales Inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash()))
        {
            throw new IllegalStateException("Credenciales inválidas");
        }

        String token = jwtUtil.generarToken(
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getId()
        );

        return AuthResponse.builder()
                .token(token)
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }
}
