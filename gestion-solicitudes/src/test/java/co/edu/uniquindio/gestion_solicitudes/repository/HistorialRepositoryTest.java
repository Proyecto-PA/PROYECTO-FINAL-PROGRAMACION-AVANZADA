package co.edu.uniquindio.gestion_solicitudes.repository;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.HistorialSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - HistorialRepository")
public class HistorialRepositoryTest {

    @Mock
    private HistorialRepository historialRepository;

    private final Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
    private final SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);

    private HistorialSolicitud buildHistorial(Long id, String accion,
                                              EstadoSolicitud anterior,
                                              EstadoSolicitud nuevo,
                                              LocalDateTime fecha) {
        return HistorialSolicitud.builder()
                .id(id)
                .solicitud(solicitud)
                .accionRealizada(accion)
                .usuarioResponsable(solicitante)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .fechaAccion(fecha)
                .build();
    }

    @Test
    @DisplayName("findBySolicitudIdOrderByFechaAccionAsc retorna historial ordenado")
    void findBySolicitudId_retornaOrdenado() {
        LocalDateTime primera = LocalDateTime.now().minusHours(2);
        LocalDateTime segunda = LocalDateTime.now().minusHours(1);
        LocalDateTime tercera = LocalDateTime.now();

        List<HistorialSolicitud> historial = List.of(
                buildHistorial(1L, "Solicitud registrada", null, EstadoSolicitud.REGISTRADA, primera),
                buildHistorial(2L, "Cambio: REGISTRADA → CLASIFICADA", EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA, segunda),
                buildHistorial(3L, "Cambio: CLASIFICADA → EN_ATENCION", EstadoSolicitud.CLASIFICADA, EstadoSolicitud.EN_ATENCION, tercera)
        );

        when(historialRepository.findBySolicitudIdOrderByFechaAccionAsc(1L))
                .thenReturn(historial);

        List<HistorialSolicitud> result =
                historialRepository.findBySolicitudIdOrderByFechaAccionAsc(1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getFechaAccion()).isEqualTo(primera);
        assertThat(result.get(1).getFechaAccion()).isEqualTo(segunda);
        assertThat(result.get(2).getFechaAccion()).isEqualTo(tercera);
        assertThat(result.get(0).getEstadoNuevo()).isEqualTo(EstadoSolicitud.REGISTRADA);
    }

    @Test
    @DisplayName("findBySolicitudIdOrderByFechaAccionAsc retorna lista vacía si no hay historial")
    void findBySolicitudId_sinHistorial_retornaVacio() {
        when(historialRepository.findBySolicitudIdOrderByFechaAccionAsc(999L))
                .thenReturn(List.of());

        List<HistorialSolicitud> result =
                historialRepository.findBySolicitudIdOrderByFechaAccionAsc(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save persiste entrada de historial correctamente")
    void save_historialValido_persiste() {
        HistorialSolicitud historial = buildHistorial(
                null, "Solicitud registrada", null,
                EstadoSolicitud.REGISTRADA, LocalDateTime.now());
        HistorialSolicitud guardado = buildHistorial(
                1L, "Solicitud registrada", null,
                EstadoSolicitud.REGISTRADA, LocalDateTime.now());

        when(historialRepository.save(historial)).thenReturn(guardado);

        HistorialSolicitud result = historialRepository.save(historial);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstadoNuevo()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(result.getEstadoAnterior()).isNull();
    }

    @Test
    @DisplayName("findBySolicitudIdOrderByFechaAccionAsc retorna solo historial de esa solicitud")
    void findBySolicitudId_retornaSoloDeLaSolicitud() {
        HistorialSolicitud entrada = buildHistorial(
                1L, "Solicitud registrada", null,
                EstadoSolicitud.REGISTRADA, LocalDateTime.now());

        when(historialRepository.findBySolicitudIdOrderByFechaAccionAsc(1L))
                .thenReturn(List.of(entrada));
        when(historialRepository.findBySolicitudIdOrderByFechaAccionAsc(2L))
                .thenReturn(List.of());

        List<HistorialSolicitud> resultSolicitud1 =
                historialRepository.findBySolicitudIdOrderByFechaAccionAsc(1L);
        List<HistorialSolicitud> resultSolicitud2 =
                historialRepository.findBySolicitudIdOrderByFechaAccionAsc(2L);

        assertThat(resultSolicitud1).hasSize(1);
        assertThat(resultSolicitud2).isEmpty();
        }
    }
