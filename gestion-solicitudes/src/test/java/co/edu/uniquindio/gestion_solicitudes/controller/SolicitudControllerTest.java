package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.CanalOrigen;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.dto.request.SolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudPageResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;
import co.edu.uniquindio.gestion_solicitudes.service.IAService;
import co.edu.uniquindio.gestion_solicitudes.service.SolicitudService;
import co.edu.uniquindio.gestion_solicitudes.util.JwtUtil;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - SolicitudController")
public class SolicitudControllerTest {

        @Mock private SolicitudService solicitudService;
        @Mock private JwtUtil jwtUtil;
        @Mock private IAService iaService;

        @InjectMocks private SolicitudController solicitudController;

        private static final String AUTH_DOCENTE   = "Bearer token.docente";
        private static final String TOKEN_DOCENTE  = "token.docente";
        private static final Long   ID_DOCENTE     = 2L;

        private SolicitudResponse buildSolicitudResponse() {
                return SolicitudResponse.builder()
                        .id(1L)
                        .tipo(TipoSolicitud.REGISTRO_ASIGNATURAS)
                        .descripcion("Descripción de prueba suficientemente larga")
                        .canalOrigen(CanalOrigen.CSU)
                        .estado(EstadoSolicitud.REGISTRADA)
                        .impactoAcademico(3)
                        .solicitante(UsuarioResumenResponse.builder()
                                .id(1L).nombre("Estudiante Test")
                                .email("est@universidad.edu").rol(RolUsuario.ESTUDIANTE)
                                .build())
                        .build();
        }

        // ----  REGISTRAR ----

        @Test
        @DisplayName("registrar - extrae userId del token y retorna 201")
        void registrar_conToken_retorna201() {
                SolicitudRequest request = TestDataFactory.crearSolicitudRequest();
                SolicitudResponse solicitudResponse = buildSolicitudResponse();
                String authHeader = "Bearer token.jwt.test";

                when(jwtUtil.extraerUserId("token.jwt.test")).thenReturn(1L);
                when(solicitudService.registrar(request, 1L)).thenReturn(solicitudResponse);

                ResponseEntity<SolicitudResponse> response = solicitudController.registrar(request, authHeader);

                assertThat(response.getStatusCode().value()).isEqualTo(201);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
                verify(jwtUtil).extraerUserId("token.jwt.test");
                verify(solicitudService).registrar(request, 1L);
        }

        @Test
        @DisplayName("obtener - solicitud existente retorna 200 con datos")
        void obtener_solicitudExistente_retorna200() {
                SolicitudResponse solicitudResponse = buildSolicitudResponse();

                when(solicitudService.obtenerPorId(1L)).thenReturn(solicitudResponse);

                ResponseEntity<SolicitudResponse> response = solicitudController.obtener(1L);

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getId()).isEqualTo(1L);
                verify(solicitudService).obtenerPorId(1L);
        }

        @Test
        @DisplayName("consultar - docente con filtro de estado retorna 200 con página")
        void consultar_conFiltroEstado_retorna200() {
                SolicitudPageResponse pageResponse = SolicitudPageResponse.builder()
                        .contenido(List.of(buildSolicitudResponse()))
                        .paginaActual(0)
                        .totalPaginas(1)
                        .totalElementos(1L)
                        .build();

                when(jwtUtil.extraerRol(TOKEN_DOCENTE)).thenReturn("DOCENTE");
                when(jwtUtil.extraerUserId(TOKEN_DOCENTE)).thenReturn(ID_DOCENTE);
                when(solicitudService.consultar(
                        EstadoSolicitud.REGISTRADA, null, null, null, null, null,
                        ID_DOCENTE, "DOCENTE", PageRequest.of(0, 10)))
                        .thenReturn(pageResponse);

                ResponseEntity<SolicitudPageResponse> response = solicitudController.consultar(
                        EstadoSolicitud.REGISTRADA, null, null, null, null, null,
                        0, 10, AUTH_DOCENTE);

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getTotalElementos()).isEqualTo(1L);
                assertThat(response.getBody().getContenido()).hasSize(1);
        }

        @Test
        @DisplayName("consultar - sin filtros retorna 200 con todas las solicitudes")
        void consultar_sinFiltros_retorna200() {
                SolicitudPageResponse pageResponse = SolicitudPageResponse.builder()
                        .contenido(List.of(buildSolicitudResponse(), buildSolicitudResponse()))
                        .paginaActual(0)
                        .totalPaginas(1)
                        .totalElementos(2L)
                        .build();

                when(jwtUtil.extraerRol(TOKEN_DOCENTE)).thenReturn("DOCENTE");
                when(jwtUtil.extraerUserId(TOKEN_DOCENTE)).thenReturn(ID_DOCENTE);
                when(solicitudService.consultar(
                        null, null, null, null, null, null,
                        ID_DOCENTE, "DOCENTE", PageRequest.of(0, 20)))
                        .thenReturn(pageResponse);

                ResponseEntity<SolicitudPageResponse> response = solicitudController.consultar(
                        null, null, null, null, null, null,
                        0, 20, AUTH_DOCENTE);

                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getTotalElementos()).isEqualTo(2L);
        }

        @Test
        @DisplayName("consultar - estudiante fuerza su propio ID como solicitanteId")
        void consultar_estudiante_fuerzaSuPropioId() {
                SolicitudPageResponse pageResponse = SolicitudPageResponse.builder()
                        .contenido(List.of(buildSolicitudResponse()))
                        .paginaActual(0).totalPaginas(1).totalElementos(1L)
                        .build();

                String authEstudiante  = "Bearer token.estudiante";
                String tokenEstudiante = "token.estudiante";
                Long   idEstudiante    = 1L;

                when(jwtUtil.extraerRol(tokenEstudiante)).thenReturn("ESTUDIANTE");
                when(jwtUtil.extraerUserId(tokenEstudiante)).thenReturn(idEstudiante);

                when(solicitudService.consultar(
                        null, null, null, null, idEstudiante, null,
                        idEstudiante, "ESTUDIANTE", PageRequest.of(0, 20)))
                        .thenReturn(pageResponse);

                ResponseEntity<SolicitudPageResponse> response = solicitudController.consultar(
                        null, null, null, null, 1L, null,
                        0, 20, authEstudiante);

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                verify(solicitudService).consultar(
                        null, null, null, null, idEstudiante, null,
                        idEstudiante, "ESTUDIANTE", PageRequest.of(0, 20));
        }

        @Test
        @DisplayName("registrar - userId extraído del token se pasa al servicio")
        void registrar_verificaUserIdDelToken() {
                SolicitudRequest request = TestDataFactory.crearSolicitudRequest();
                String authHeader = "Bearer token.diferente";

                when(jwtUtil.extraerUserId("token.diferente")).thenReturn(5L);
                when(solicitudService.registrar(request, 5L)).thenReturn(buildSolicitudResponse());

                solicitudController.registrar(request, authHeader);

                verify(jwtUtil).extraerUserId("token.diferente");
                verify(solicitudService).registrar(request, 5L);
        }
}