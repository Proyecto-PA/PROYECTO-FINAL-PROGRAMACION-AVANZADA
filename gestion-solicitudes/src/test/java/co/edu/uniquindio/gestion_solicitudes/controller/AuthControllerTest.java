package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import co.edu.uniquindio.gestion_solicitudes.dto.request.LoginRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.RegistroRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.AuthResponse;
import co.edu.uniquindio.gestion_solicitudes.service.AuthService;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - AuthController")
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthResponse buildAuthResponse(String email, RolUsuario rol) {
        return AuthResponse.builder()
                .token("token.jwt.test")
                .usuarioId(1L)
                .nombre("Usuario Test")
                .email(email)
                .rol(rol)
                .build();
    }

    @Test
    @DisplayName("registro - llama al servicio y retorna 201 con token")
    void registro_llamaServicioYRetorna201() {
        RegistroRequest request = TestDataFactory.crearRegistroRequest(
                "nuevo@universidad.edu", RolUsuario.ESTUDIANTE);
        AuthResponse authResponse = buildAuthResponse("nuevo@universidad.edu", RolUsuario.ESTUDIANTE);

        when(authService.registro(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.registro(request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("token.jwt.test");
        assertThat(response.getBody().getEmail()).isEqualTo("nuevo@universidad.edu");
        verify(authService).registro(request);
    }

    @Test
    @DisplayName("login - llama al servicio y retorna 200 con token")
    void login_llamaServicioYRetorna200() {
        LoginRequest request = TestDataFactory.crearLoginRequest("test@universidad.edu");
        AuthResponse authResponse = buildAuthResponse("test@universidad.edu", RolUsuario.ESTUDIANTE);

        when(authService.login(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("token.jwt.test");
        verify(authService).login(request);
    }

    @Test
    @DisplayName("registro - delega completamente en el servicio sin lógica propia")
    void registro_delegaEnServicio() {
        RegistroRequest request = TestDataFactory.crearRegistroRequest(
                "otro@universidad.edu", RolUsuario.DOCENTE);
        AuthResponse authResponse = buildAuthResponse("otro@universidad.edu", RolUsuario.DOCENTE);

        when(authService.registro(request)).thenReturn(authResponse);

        authController.registro(request);

        verify(authService, times(1)).registro(request);
        verifyNoMoreInteractions(authService);
    }

    @Test
    @DisplayName("login - delega completamente en el servicio sin lógica propia")
    void login_delegaEnServicio() {
        LoginRequest request = TestDataFactory.crearLoginRequest("test@universidad.edu");
        AuthResponse authResponse = buildAuthResponse("test@universidad.edu", RolUsuario.ADMINISTRATIVO);

        when(authService.login(request)).thenReturn(authResponse);

        authController.login(request);

        verify(authService, times(1)).login(request);
        verifyNoMoreInteractions(authService);
    }
}
