package co.edu.uniquindio.gestion_solicitudes.service;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.HistorialSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.SolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudPageResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.HistorialRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.SolicitudRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.UsuarioRepository;
import co.edu.uniquindio.gestion_solicitudes.service.impl.SolicitudServiceImpl;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - SolicitudService")
public class SolicitudServiceTest {
    @Mock private SolicitudRepository solicitudRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialRepository historialRepository;

    @InjectMocks private SolicitudServiceImpl solicitudService;

    // ── REGISTRAR ─────────────────────────────────────────────

    @Test
    @DisplayName("Registrar solicitud válida devuelve respuesta con estado REGISTRADA")
    void registrar_solicitudValida_devuelveRegistrada() {
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudRequest request = TestDataFactory.crearSolicitudRequest();
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        when(solicitudRepository.save(any())).thenReturn(solicitud);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        SolicitudResponse response = solicitudService.registrar(request, 1L);

        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(response.getSolicitante().getId()).isEqualTo(1L);
        verify(historialRepository).save(any(HistorialSolicitud.class));
    }

    @Test
    @DisplayName("Registrar con usuario inexistente lanza RecursoNoEncontradoException")
    void registrar_usuarioInexistente_lanzaExcepcion() {
        SolicitudRequest request = TestDataFactory.crearSolicitudRequest();

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.registrar(request, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(solicitudRepository, never()).save(any());
    }

    // ── OBTENER POR ID ────────────────────────────────────────

    @Test
    @DisplayName("Obtener solicitud existente devuelve los datos correctos")
    void obtenerPorId_existe_devuelveSolicitud() {
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        SolicitudResponse response = solicitudService.obtenerPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTipo()).isEqualTo(TipoSolicitud.REGISTRO_ASIGNATURAS);
    }

    @Test
    @DisplayName("Obtener solicitud inexistente lanza RecursoNoEncontradoException")
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(solicitudRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.obtenerPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ── CONSULTAR CON FILTROS ─────────────────────────────────

    @Test
    @DisplayName("Consultar solicitudes devuelve página con contenido correcto")
    void consultar_devuelvePaginaConSolicitudes() {
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);
        Page<SolicitudAcademica> page = new PageImpl<>(List.of(solicitud));

        when(solicitudRepository.findWithFilters(any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        SolicitudPageResponse response = solicitudService.consultar(
                null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(response.getTotalElementos()).isEqualTo(1);
        assertThat(response.getContenido()).hasSize(1);
    }

    @Test
    @DisplayName("Consultar sin resultados devuelve página vacía")
    void consultar_sinResultados_devuelvePaginaVacia() {
        Page<SolicitudAcademica> page = new PageImpl<>(List.of());

        when(solicitudRepository.findWithFilters(any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        SolicitudPageResponse response = solicitudService.consultar(
                null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(response.getTotalElementos()).isZero();
        assertThat(response.getContenido()).isEmpty();
    }
}
