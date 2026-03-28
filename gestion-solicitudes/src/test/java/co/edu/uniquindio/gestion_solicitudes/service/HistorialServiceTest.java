package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.response.HistorialSolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.*;
import co.edu.uniquindio.gestion_solicitudes.service.impl.HistorialServiceImpl;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - HistorialService")
public class HistorialServiceTest {

    @Mock
    private HistorialRepository historialRepository;
    @Mock private SolicitudRepository solicitudRepository;

    @InjectMocks private HistorialServiceImpl historialService;

    private final Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
    private final SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);

    private HistorialSolicitud buildHistorial(Long id, String accion,
                                              EstadoSolicitud anterior,
                                              EstadoSolicitud nuevo) {
        return HistorialSolicitud.builder()
                .id(id)
                .solicitud(solicitud)
                .accionRealizada(accion)
                .usuarioResponsable(solicitante)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .fechaAccion(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("consultarPorSolicitud - retorna historial ordenado cronológicamente")
    void consultarPorSolicitud_retornaHistorialOrdenado() {
        List<HistorialSolicitud> historial = List.of(
                buildHistorial(1L, "Solicitud registrada",
                        null, EstadoSolicitud.REGISTRADA),
                buildHistorial(2L, "Cambio: REGISTRADA → CLASIFICADA",
                        EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA)
        );

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(historialRepository.findBySolicitudIdOrderByFechaAccionAsc(1L))
                .thenReturn(historial);

        List<HistorialSolicitudResponse> result =
                historialService.consultarPorSolicitud(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEstadoNuevo()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(result.get(1).getEstadoAnterior()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(result.get(1).getEstadoNuevo()).isEqualTo(EstadoSolicitud.CLASIFICADA);
    }

    @Test
    @DisplayName("consultarPorSolicitud - primera entrada tiene estadoAnterior null")
    void consultarPorSolicitud_primeraEntrada_estadoAnteriorNull() {
        List<HistorialSolicitud> historial = List.of(
                buildHistorial(1L, "Solicitud registrada",
                        null, EstadoSolicitud.REGISTRADA)
        );

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(historialRepository.findBySolicitudIdOrderByFechaAccionAsc(1L))
                .thenReturn(historial);

        List<HistorialSolicitudResponse> result =
                historialService.consultarPorSolicitud(1L);

        assertThat(result.get(0).getEstadoAnterior()).isNull();
        assertThat(result.get(0).getEstadoNuevo()).isEqualTo(EstadoSolicitud.REGISTRADA);
    }

    @Test
    @DisplayName("consultarPorSolicitud - solicitud inexistente lanza RecursoNoEncontradoException")
    void consultarPorSolicitud_solicitudInexistente_lanzaExcepcion() {
        when(solicitudRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historialService.consultarPorSolicitud(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(historialRepository, never())
                .findBySolicitudIdOrderByFechaAccionAsc(any());
    }

    @Test
    @DisplayName("consultarPorSolicitud - sin entradas retorna lista vacía")
    void consultarPorSolicitud_sinEntradas_retornaVacio() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(historialRepository.findBySolicitudIdOrderByFechaAccionAsc(1L))
                .thenReturn(List.of());

        List<HistorialSolicitudResponse> result =
                historialService.consultarPorSolicitud(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("consultarPorSolicitud - mapea correctamente el usuarioResponsable")
    void consultarPorSolicitud_mapeaUsuarioResponsable() {
        List<HistorialSolicitud> historial = List.of(
                buildHistorial(1L, "Solicitud registrada",
                        null, EstadoSolicitud.REGISTRADA)
        );

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(historialRepository.findBySolicitudIdOrderByFechaAccionAsc(1L))
                .thenReturn(historial);

        List<HistorialSolicitudResponse> result =
                historialService.consultarPorSolicitud(1L);

        assertThat(result.get(0).getUsuarioResponsable()).isNotNull();
        assertThat(result.get(0).getUsuarioResponsable().getId()).isEqualTo(1L);
        assertThat(result.get(0).getUsuarioResponsable().getRol())
                .isEqualTo(RolUsuario.ESTUDIANTE);
    }
}
