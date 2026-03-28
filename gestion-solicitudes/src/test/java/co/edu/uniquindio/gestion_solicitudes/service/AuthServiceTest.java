package co.edu.uniquindio.gestion_solicitudes.service;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import co.edu.uniquindio.gestion_solicitudes.dto.request.LoginRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.RegistroRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.AuthResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.CredencialesInvalidasException;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.UsuarioRepository;
import co.edu.uniquindio.gestion_solicitudes.util.JwtUtil;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - AuthService")
public class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("Registro exitoso devuelve token y datos del usuario")
    void registro_exitoso() {
        RegistroRequest request = TestDataFactory.crearRegistroRequest(
                "nuevo@universidad.edu", RolUsuario.ESTUDIANTE);

        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(inv -> {
                    Usuario u = inv.getArgument(0);
                    u = Usuario.builder()
                            .id(1L).nombre(u.getNombre()).email(u.getEmail())
                            .passwordHash(u.getPasswordHash()).rol(u.getRol()).activo(true)
                            .build();
                    return u;
                });
        when(jwtUtil.generarToken(anyString(), anyString(), anyLong()))
                .thenReturn("token.jwt.generado");

        AuthResponse response = authService.registro(request);

        assertThat(response.getToken()).isEqualTo("token.jwt.generado");
        assertThat(response.getEmail()).isEqualTo("nuevo@universidad.edu");
        assertThat(response.getRol()).isEqualTo(RolUsuario.ESTUDIANTE);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro con email duplicado lanza IllegalStateException")
    void registro_emailDuplicado_lanzaExcepcion() {
        RegistroRequest request = TestDataFactory.crearRegistroRequest(
                "existe@universidad.edu", RolUsuario.ESTUDIANTE);
        Usuario existente = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);

        when(usuarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> authService.registro(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ya existe un usuario");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login exitoso devuelve token")
    void login_exitoso() {
        LoginRequest request = TestDataFactory.crearLoginRequest("test1@universidad.edu");
        Usuario usuario = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);

        when(usuarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash()))
                .thenReturn(true);
        when(jwtUtil.generarToken(anyString(), anyString(), anyLong()))
                .thenReturn("token.jwt.login");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("token.jwt.login");
        assertThat(response.getEmail()).isEqualTo(usuario.getEmail());
    }

    @Test
    @DisplayName("Login con email inexistente lanza excepción")
    void login_emailInexistente_lanzaExcepcion() {
        LoginRequest request = TestDataFactory.crearLoginRequest("noexiste@universidad.edu");

        when(usuarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Login con contraseña incorrecta lanza CredencialesInvalidasException")
    void login_passwordIncorrecta_lanzaExcepcion() {
        LoginRequest request = TestDataFactory.crearLoginRequest("test1@universidad.edu");
        Usuario usuario = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);

        when(usuarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash()))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    @DisplayName("Login con usuario inactivo lanza IllegalStateException")
    void login_usuarioInactivo_lanzaExcepcion() {
        LoginRequest request = TestDataFactory.crearLoginRequest("test1@universidad.edu");
        Usuario usuario = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        usuario.setActivo(false);

        when(usuarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está activo");
    }

}
