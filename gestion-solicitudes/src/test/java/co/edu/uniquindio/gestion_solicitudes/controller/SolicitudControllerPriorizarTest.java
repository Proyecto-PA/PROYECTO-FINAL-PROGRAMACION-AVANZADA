package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
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
@DisplayName("Tests unitarios - SolicitudController.priorizar")
class SolicitudControllerPriorizarTest {

    @Mock private SolicitudService solicitudService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private SolicitudController solicitudController;

    private SolicitudResponse buildPriorizadaResponse(Prioridad prioridad, String justificacion) {
        return SolicitudResponse.builder()
            .id(1L)
            .tipo(TipoSolicitud.HOMOLOGACION)
            .descripcion("Descripción de prueba suficientemente larga")
            .canalOrigen(CanalOrigen.CSU)
            .estado(EstadoSolicitud.REGISTRADA)
            .prioridad(prioridad)
            .justificacionPrioridad(justificacion)
            .impactoAcademico(5)
            .solicitante(UsuarioResumenResponse.builder()
                .id(1L).nombre("Estudiante Test")
                .email("est@universidad.edu").rol(RolUsuario.ESTUDIANTE)
                .build())
            .build();
    }

    @Test
    @DisplayName("priorizar - retorna 200 con prioridad asignada")
    void priorizar_exitoso_retorna200() {
        String authHeader = "Bearer token.admin";
        SolicitudResponse response = buildPriorizadaResponse(Prioridad.CRITICA, "Justificación crítica.");

        when(jwtUtil.extraerUserId("token.admin")).thenReturn(1L);
        when(solicitudService.priorizar(1L, 1L)).thenReturn(response);

        ResponseEntity<SolicitudResponse> result = solicitudController.priorizar(1L, authHeader);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getPrioridad()).isEqualTo(Prioridad.CRITICA);
        assertThat(result.getBody().getJustificacionPrioridad()).isNotBlank();
        verify(solicitudService).priorizar(1L, 1L);
    }

    @Test
    @DisplayName("priorizar - extrae userId del token correctamente")
    void priorizar_extraeUserIdDelToken() {
        String authHeader = "Bearer token.docente";

        when(jwtUtil.extraerUserId("token.docente")).thenReturn(5L);
        when(solicitudService.priorizar(1L, 5L))
            .thenReturn(buildPriorizadaResponse(Prioridad.ALTA, "Alta prioridad."));

        solicitudController.priorizar(1L, authHeader);

        verify(jwtUtil).extraerUserId("token.docente");
        verify(solicitudService).priorizar(1L, 5L);
    }

    @Test
    @DisplayName("priorizar - solicitud inexistente propaga ResourceNotFoundException")
    void priorizar_solicitudInexistente_propagaExcepcion() {
        String authHeader = "Bearer token.admin";

        when(jwtUtil.extraerUserId("token.admin")).thenReturn(1L);
        when(solicitudService.priorizar(999L, 1L))
            .thenThrow(new ResourceNotFoundException("No existe una solicitud con id 999"));

        assertThatThrownBy(() -> solicitudController.priorizar(999L, authHeader))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    @DisplayName("priorizar - delega completamente en el servicio")
    void priorizar_delegaEnServicio() {
        String authHeader = "Bearer mi.token";

        when(jwtUtil.extraerUserId("mi.token")).thenReturn(3L);
        when(solicitudService.priorizar(1L, 3L))
            .thenReturn(buildPriorizadaResponse(Prioridad.MEDIA, "Media prioridad."));

        solicitudController.priorizar(1L, authHeader);

        verify(solicitudService, times(1)).priorizar(1L, 3L);
        verifyNoMoreInteractions(solicitudService);
    }

    @Test
    @DisplayName("priorizar - respuesta incluye justificacion de prioridad")
    void priorizar_respuestaConJustificacion() {
        String authHeader = "Bearer token.admin";
        String justificacion = "- Impacto académico nivel 5/5. → CRITICA. Prioridad final calculada: CRITICA.";
        SolicitudResponse response = buildPriorizadaResponse(Prioridad.CRITICA, justificacion);

        when(jwtUtil.extraerUserId("token.admin")).thenReturn(1L);
        when(solicitudService.priorizar(1L, 1L)).thenReturn(response);

        ResponseEntity<SolicitudResponse> result = solicitudController.priorizar(1L, authHeader);

        assertThat(result.getBody().getJustificacionPrioridad()).contains("CRITICA");
    }
}
