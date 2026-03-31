package co.edu.uniquindio.gestion_solicitudes.controller;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest;
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
@DisplayName("Tests unitarios - SolicitudController.clasificar")

public class SolicitudControllerClasificarTest {

    @Mock private SolicitudService solicitudService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private SolicitudController solicitudController;

    private SolicitudResponse buildClasificadaResponse() {
        return SolicitudResponse.builder()
            .id(1L)
            .tipo(TipoSolicitud.HOMOLOGACION)
            .descripcion("Descripción de prueba suficientemente larga")
            .canalOrigen(CanalOrigen.CSU)
            .estado(EstadoSolicitud.CLASIFICADA)
            .impactoAcademico(3)
            .solicitante(UsuarioResumenResponse.builder()
                .id(1L).nombre("Estudiante Test")
                .email("est@universidad.edu").rol(RolUsuario.ESTUDIANTE)
                .build())
            .build();
    }

    private ClasificarRequest crearRequest(TipoSolicitud tipo) {
        ClasificarRequest r = new ClasificarRequest();
        r.setTipo(tipo);
        r.setObservacion("Observación de prueba");
        return r;
    }

    @Test
    @DisplayName("clasificar - retorna 200 con solicitud clasificada")
    void clasificar_exitoso_retorna200() {
        ClasificarRequest request = crearRequest(TipoSolicitud.HOMOLOGACION);
        SolicitudResponse response = buildClasificadaResponse();
        String authHeader = "Bearer token.docente";

        when(jwtUtil.extraerUserId("token.docente")).thenReturn(2L);
        when(solicitudService.clasificar(1L, request, 2L)).thenReturn(response);

        ResponseEntity<SolicitudResponse> result =
            solicitudController.clasificar(1L, request, authHeader);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getEstado()).isEqualTo(EstadoSolicitud.CLASIFICADA);
        assertThat(result.getBody().getTipo()).isEqualTo(TipoSolicitud.HOMOLOGACION);
        verify(jwtUtil).extraerUserId("token.docente");
        verify(solicitudService).clasificar(1L, request, 2L);
    }

    @Test
    @DisplayName("clasificar - extrae userId del token correctamente")
    void clasificar_extraeUserIdDelToken() {
        ClasificarRequest request = crearRequest(TipoSolicitud.HOMOLOGACION);
        String authHeader = "Bearer token.diferente";

        when(jwtUtil.extraerUserId("token.diferente")).thenReturn(7L);
        when(solicitudService.clasificar(1L, request, 7L))
            .thenReturn(buildClasificadaResponse());

        solicitudController.clasificar(1L, request, authHeader);

        verify(jwtUtil).extraerUserId("token.diferente");
        verify(solicitudService).clasificar(1L, request, 7L);
    }

    @Test
    @DisplayName("clasificar - solicitud inexistente propaga excepción del servicio")
    void clasificar_solicitudInexistente_propagaExcepcion() {
        ClasificarRequest request = crearRequest(TipoSolicitud.HOMOLOGACION);
        String authHeader = "Bearer token.docente";

        when(jwtUtil.extraerUserId("token.docente")).thenReturn(2L);
        when(solicitudService.clasificar(999L, request, 2L))
            .thenThrow(new ResourceNotFoundException("No existe una solicitud con id 999"));

        assertThatThrownBy(() ->
            solicitudController.clasificar(999L, request, authHeader))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    @DisplayName("clasificar - transición inválida propaga IllegalStateException del servicio")
    void clasificar_transicionInvalida_propagaExcepcion() {
        ClasificarRequest request = crearRequest(TipoSolicitud.OTRO);
        String authHeader = "Bearer token.docente";

        when(jwtUtil.extraerUserId("token.docente")).thenReturn(2L);
        when(solicitudService.clasificar(1L, request, 2L))
            .thenThrow(new IllegalStateException("No es posible pasar de CLASIFICADA a CLASIFICADA"));

        assertThatThrownBy(() ->
            solicitudController.clasificar(1L, request, authHeader))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("clasificar - delega completamente en el servicio")
    void clasificar_delegaEnServicio() {
        ClasificarRequest request = crearRequest(TipoSolicitud.REGISTRO_ASIGNATURAS);
        String authHeader = "Bearer mi.token";

        when(jwtUtil.extraerUserId("mi.token")).thenReturn(3L);
        when(solicitudService.clasificar(1L, request, 3L))
            .thenReturn(buildClasificadaResponse());

        solicitudController.clasificar(1L, request, authHeader);

        verify(solicitudService, times(1)).clasificar(1L, request, 3L);
        verifyNoMoreInteractions(solicitudService);
    }
    
}
