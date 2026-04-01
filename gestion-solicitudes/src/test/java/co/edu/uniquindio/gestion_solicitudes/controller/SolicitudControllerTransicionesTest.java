package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CambiarEstadoRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CerrarSolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;
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
@DisplayName("Tests unitarios - SolicitudController transiciones de estado")
public class SolicitudControllerTransicionesTest {

    @Mock private SolicitudService solicitudService;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private SolicitudController solicitudController;

    private SolicitudResponse buildResponse(EstadoSolicitud estado) {
        return SolicitudResponse.builder()
            .id(1L).estado(estado)
            .tipo(TipoSolicitud.HOMOLOGACION)
            .canalOrigen(CanalOrigen.CSU)
            .descripcion("Descripción de prueba")
            .solicitante(UsuarioResumenResponse.builder()
                .id(1L).nombre("Test").email("test@uni.edu")
                .rol(RolUsuario.ESTUDIANTE).build())
            .build();
    }

    private CambiarEstadoRequest requestObs(String obs) {
        CambiarEstadoRequest r = new CambiarEstadoRequest();
        r.setObservacion(obs);
        return r;
    }

    private CerrarSolicitudRequest requestCierre(String obs) {
        CerrarSolicitudRequest r = new CerrarSolicitudRequest();
        r.setObservacion(obs);
        return r;
    }

    @Test
    @DisplayName("iniciarAtencion - retorna 200 con estado EN_ATENCION")
    void iniciarAtencion_retorna200() {
        CambiarEstadoRequest req = requestObs("Iniciando");
        String authHeader = "Bearer token.docente";

        when(jwtUtil.extraerUserId("token.docente")).thenReturn(2L);
        when(solicitudService.iniciarAtencion(1L, req, 2L))
            .thenReturn(buildResponse(EstadoSolicitud.EN_ATENCION));

        ResponseEntity<SolicitudResponse> result =
            solicitudController.iniciarAtencion(1L, req, authHeader);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getEstado()).isEqualTo(EstadoSolicitud.EN_ATENCION);
        verify(solicitudService).iniciarAtencion(1L, req, 2L);
    }

    @Test
    @DisplayName("marcarAtendida - retorna 200 con estado ATENDIDA")
    void marcarAtendida_retorna200() {
        CambiarEstadoRequest req = requestObs("Resuelto");
        String authHeader = "Bearer token.docente";

        when(jwtUtil.extraerUserId("token.docente")).thenReturn(2L);
        when(solicitudService.marcarAtendida(1L, req, 2L))
            .thenReturn(buildResponse(EstadoSolicitud.ATENDIDA));

        ResponseEntity<SolicitudResponse> result =
            solicitudController.marcarAtendida(1L, req, authHeader);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getEstado()).isEqualTo(EstadoSolicitud.ATENDIDA);
    }

    @Test
    @DisplayName("cerrar - retorna 200 con estado CERRADA")
    void cerrar_retorna200() {
        CerrarSolicitudRequest req = requestCierre("Solicitud resuelta con éxito.");
        String authHeader = "Bearer token.docente";

        when(jwtUtil.extraerUserId("token.docente")).thenReturn(2L);
        when(solicitudService.cerrar(1L, req, 2L))
            .thenReturn(buildResponse(EstadoSolicitud.CERRADA));

        ResponseEntity<SolicitudResponse> result =
            solicitudController.cerrar(1L, req, authHeader);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getEstado()).isEqualTo(EstadoSolicitud.CERRADA);
    }

    @Test
    @DisplayName("cancelar - retorna 200 con estado CANCELADA")
    void cancelar_retorna200() {
        CambiarEstadoRequest req = requestObs("Error en solicitud");
        String authHeader = "Bearer token.estudiante";

        when(jwtUtil.extraerUserId("token.estudiante")).thenReturn(1L);
        when(solicitudService.cancelar(1L, req, 1L))
            .thenReturn(buildResponse(EstadoSolicitud.CANCELADA));

        ResponseEntity<SolicitudResponse> result =
            solicitudController.cancelar(1L, req, authHeader);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getEstado()).isEqualTo(EstadoSolicitud.CANCELADA);
    }

    @Test
    @DisplayName("iniciarAtencion - transicion invalida propaga excepcion del servicio")
    void iniciarAtencion_transicionInvalida_propagaExcepcion() {
        CambiarEstadoRequest req = requestObs("obs");
        String authHeader = "Bearer token.docente";

        when(jwtUtil.extraerUserId("token.docente")).thenReturn(2L);
        when(solicitudService.iniciarAtencion(1L, req, 2L))
            .thenThrow(new IllegalStateException("No es posible pasar de REGISTRADA a EN_ATENCION"));

        assertThatThrownBy(() ->
            solicitudController.iniciarAtencion(1L, req, authHeader))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("todos los endpoints extraen userId del token correctamente")
    void todosExtraenUserIdDelToken() {
        String authHeader = "Bearer token.test";
        when(jwtUtil.extraerUserId("token.test")).thenReturn(5L);
        when(solicitudService.iniciarAtencion(eq(1L), any(), eq(5L)))
            .thenReturn(buildResponse(EstadoSolicitud.EN_ATENCION));

        solicitudController.iniciarAtencion(1L, requestObs("obs"), authHeader);

        verify(jwtUtil, times(1)).extraerUserId("token.test");
        verify(solicitudService).iniciarAtencion(eq(1L), any(), eq(5L));
    }
}