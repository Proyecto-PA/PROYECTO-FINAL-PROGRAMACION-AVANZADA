package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.AsignarResponsableRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.service.SolicitudService;
import co.edu.uniquindio.gestion_solicitudes.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - SolicitudController.asignarResponsable")
class SolicitudControllerResponsableTest {

    @Mock private SolicitudService solicitudService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private SolicitudController solicitudController;

    private SolicitudResponse buildResponseConResponsable() {
        return SolicitudResponse.builder()
            .id(1L)
            .tipo(TipoSolicitud.HOMOLOGACION)
            .descripcion("Descripción de prueba suficientemente larga")
            .canalOrigen(CanalOrigen.CSU)
            .estado(EstadoSolicitud.REGISTRADA)
            .impactoAcademico(3)
            .solicitante(UsuarioResumenResponse.builder()
                .id(3L).nombre("Estudiante Test")
                .email("est@universidad.edu").rol(RolUsuario.ESTUDIANTE).build())
            .responsable(UsuarioResumenResponse.builder()
                .id(2L).nombre("Docente Test")
                .email("doc@universidad.edu").rol(RolUsuario.DOCENTE).build())
            .build();
    }

    private AsignarResponsableRequest crearRequest(Long responsableId) {
        AsignarResponsableRequest r = new AsignarResponsableRequest();
        r.setResponsableId(responsableId);
        return r;
    }

    @Test
    @DisplayName("asignarResponsable - retorna 200 con responsable asignado")
    void asignarResponsable_exitoso_retorna200() {
        AsignarResponsableRequest request = crearRequest(2L);
        String authHeader = "Bearer token.admin";

        when(jwtUtil.extraerUserId("token.admin")).thenReturn(1L);
        when(solicitudService.asignarResponsable(1L, request, 1L))
            .thenReturn(buildResponseConResponsable());

        ResponseEntity<SolicitudResponse> result =
            solicitudController.asignarResponsable(1L, request, authHeader);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getResponsable()).isNotNull();
        assertThat(result.getBody().getResponsable().getId()).isEqualTo(2L);
        verify(solicitudService).asignarResponsable(1L, request, 1L);
    }

    @Test
    @DisplayName("asignarResponsable - extrae userId del token correctamente")
    void asignarResponsable_extraeUserIdDelToken() {
        AsignarResponsableRequest request = crearRequest(2L);
        String authHeader = "Bearer otro.token";

        when(jwtUtil.extraerUserId("otro.token")).thenReturn(9L);
        when(solicitudService.asignarResponsable(1L, request, 9L))
            .thenReturn(buildResponseConResponsable());

        solicitudController.asignarResponsable(1L, request, authHeader);

        verify(jwtUtil).extraerUserId("otro.token");
        verify(solicitudService).asignarResponsable(1L, request, 9L);
    }

    @Test
    @DisplayName("asignarResponsable - solicitud inexistente propaga ResourceNotFoundException")
    void asignarResponsable_solicitudInexistente_propagaExcepcion() {
        AsignarResponsableRequest request = crearRequest(2L);
        String authHeader = "Bearer token.admin";

        when(jwtUtil.extraerUserId("token.admin")).thenReturn(1L);
        when(solicitudService.asignarResponsable(999L, request, 1L))
            .thenThrow(new ResourceNotFoundException("No existe una solicitud con id 999"));

        assertThatThrownBy(() -> solicitudController.asignarResponsable(999L, request, authHeader))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    @DisplayName("asignarResponsable - responsable inactivo propaga IllegalStateException")
    void asignarResponsable_responsableInactivo_propagaExcepcion() {
        AsignarResponsableRequest request = crearRequest(2L);
        String authHeader = "Bearer token.admin";

        when(jwtUtil.extraerUserId("token.admin")).thenReturn(1L);
        when(solicitudService.asignarResponsable(1L, request, 1L))
            .thenThrow(new IllegalStateException("El usuario con id 2 no está activo"));

        assertThatThrownBy(() -> solicitudController.asignarResponsable(1L, request, authHeader))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no está activo");
    }

    @Test
    @DisplayName("asignarResponsable - delega completamente en el servicio")
    void asignarResponsable_delegaEnServicio() {
        AsignarResponsableRequest request = crearRequest(2L);
        String authHeader = "Bearer mi.token";

        when(jwtUtil.extraerUserId("mi.token")).thenReturn(1L);
        when(solicitudService.asignarResponsable(1L, request, 1L))
            .thenReturn(buildResponseConResponsable());

        solicitudController.asignarResponsable(1L, request, authHeader);

        verify(solicitudService, times(1)).asignarResponsable(1L, request, 1L);
        verifyNoMoreInteractions(solicitudService);
    }
}
