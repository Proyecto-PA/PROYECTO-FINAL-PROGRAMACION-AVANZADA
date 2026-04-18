package co.edu.uniquindio.gestion_solicitudes.repository;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.HistorialSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de integración - HistorialRepository")
class HistorialRepositoryTest {

    @Autowired private HistorialRepository historialRepository;
    @Autowired private SolicitudRepository solicitudRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario solicitante;
    private SolicitudAcademica solicitud;

    @BeforeEach
    void setUp() {
        solicitante = usuarioRepository.save(Usuario.builder()
                .nombre("Estudiante Test")
                .email("est@universidad.edu")
                .passwordHash("hash")
                .rol(RolUsuario.ESTUDIANTE)
                .activo(true)
                .build());

        solicitud = solicitudRepository.save(SolicitudAcademica.builder()
                .tipo(TipoSolicitud.HOMOLOGACION)
                .descripcion("Descripción de prueba suficientemente larga para pasar validación")
                .canalOrigen(CanalOrigen.CSU)
                .fechaRegistro(LocalDateTime.now())
                .estado(EstadoSolicitud.REGISTRADA)
                .solicitante(solicitante)
                .build());
    }

    // ----helper ----

    private HistorialSolicitud buildHistorial(String accion,
                                              EstadoSolicitud anterior,
                                              EstadoSolicitud nuevo,
                                              LocalDateTime fecha) {
        return HistorialSolicitud.builder()
                .solicitud(solicitud)
                .accionRealizada(accion)
                .usuarioResponsable(solicitante)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .fechaAccion(fecha)
                .build();
    }

    // ---- save ----

    @Test
    @DisplayName("save persiste la entrada y genera ID")
    void save_historialValido_persisteYGeneraId() {
        HistorialSolicitud historial = buildHistorial(
                "Solicitud registrada",
                null,
                EstadoSolicitud.REGISTRADA,
                LocalDateTime.now());

        HistorialSolicitud guardado = historialRepository.save(historial);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getEstadoNuevo()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(guardado.getEstadoAnterior()).isNull();
    }

    // ---- findBySolicitudIdOrderByFechaAccionAsc ----

    @Test
    @DisplayName("retorna historial ordenado cronológicamente ascendente")
    void findBySolicitudId_retornaOrdenadoPorFecha() {
        LocalDateTime tercera = LocalDateTime.now();
        LocalDateTime segunda = tercera.minusMinutes(30);
        LocalDateTime primera = tercera.minusHours(1);

        historialRepository.save(buildHistorial(
                "Cambio a EN_ATENCION",
                EstadoSolicitud.CLASIFICADA, EstadoSolicitud.EN_ATENCION, tercera));
        historialRepository.save(buildHistorial(
                "Cambio a CLASIFICADA",
                EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA, segunda));
        historialRepository.save(buildHistorial(
                "Solicitud registrada",
                null, EstadoSolicitud.REGISTRADA, primera));

        List<HistorialSolicitud> result =
                historialRepository.findBySolicitudIdOrderByFechaAccionAsc(solicitud.getId());

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getFechaAccion()).isEqualTo(primera);
        assertThat(result.get(1).getFechaAccion()).isEqualTo(segunda);
        assertThat(result.get(2).getFechaAccion()).isEqualTo(tercera);
    }

    @Test
    @DisplayName("primera entrada del historial tiene estadoAnterior null")
    void findBySolicitudId_primeraEntrada_estadoAnteriorNull() {
        historialRepository.save(buildHistorial(
                "Solicitud registrada",
                null, EstadoSolicitud.REGISTRADA, LocalDateTime.now()));

        List<HistorialSolicitud> result =
                historialRepository.findBySolicitudIdOrderByFechaAccionAsc(solicitud.getId());

        assertThat(result.get(0).getEstadoAnterior()).isNull();
        assertThat(result.get(0).getEstadoNuevo()).isEqualTo(EstadoSolicitud.REGISTRADA);
    }

    @Test
    @DisplayName("retorna lista vacía si la solicitud no tiene historial")
    void findBySolicitudId_sinHistorial_retornaListaVacia() {
        SolicitudAcademica otraSolicitud = solicitudRepository.save(
                SolicitudAcademica.builder()
                        .tipo(TipoSolicitud.CONSULTA_ACADEMICA)
                        .descripcion("Descripción de prueba suficientemente larga para pasar validación")
                        .canalOrigen(CanalOrigen.CORREO)
                        .fechaRegistro(LocalDateTime.now())
                        .estado(EstadoSolicitud.REGISTRADA)
                        .solicitante(solicitante)
                        .build());

        List<HistorialSolicitud> result =
                historialRepository.findBySolicitudIdOrderByFechaAccionAsc(otraSolicitud.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("no devuelve historial de otras solicitudes")
    void findBySolicitudId_noMezclaConOtrasSolicitudes() {
        SolicitudAcademica otraSolicitud = solicitudRepository.save(
                SolicitudAcademica.builder()
                        .tipo(TipoSolicitud.SOLICITUD_CUPOS)
                        .descripcion("Descripción de prueba suficientemente larga para pasar validación")
                        .canalOrigen(CanalOrigen.SAC)
                        .fechaRegistro(LocalDateTime.now())
                        .estado(EstadoSolicitud.REGISTRADA)
                        .solicitante(solicitante)
                        .build());

        historialRepository.save(buildHistorial(
                "Solicitud registrada",
                null, EstadoSolicitud.REGISTRADA, LocalDateTime.now()));

        historialRepository.save(HistorialSolicitud.builder()
                .solicitud(otraSolicitud)
                .accionRealizada("Otra solicitud registrada")
                .usuarioResponsable(solicitante)
                .estadoAnterior(null)
                .estadoNuevo(EstadoSolicitud.REGISTRADA)
                .fechaAccion(LocalDateTime.now())
                .build());

        List<HistorialSolicitud> result =
                historialRepository.findBySolicitudIdOrderByFechaAccionAsc(solicitud.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSolicitud().getId()).isEqualTo(solicitud.getId());
    }

    @Test
    @DisplayName("persiste correctamente todos los estados del ciclo de vida")
    void save_cicloDeVidaCompleto_persisteTodosLosEstados() {
        LocalDateTime base = LocalDateTime.now();

        historialRepository.save(buildHistorial("Registrada", null, EstadoSolicitud.REGISTRADA, base));
        historialRepository.save(buildHistorial("Clasificada",
                EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA, base.plusMinutes(1)));
        historialRepository.save(buildHistorial("En atención",
                EstadoSolicitud.CLASIFICADA, EstadoSolicitud.EN_ATENCION, base.plusMinutes(2)));
        historialRepository.save(buildHistorial("Atendida",
                EstadoSolicitud.EN_ATENCION, EstadoSolicitud.ATENDIDA, base.plusMinutes(3)));
        historialRepository.save(buildHistorial("Cerrada",
                EstadoSolicitud.ATENDIDA, EstadoSolicitud.CERRADA, base.plusMinutes(4)));

        List<HistorialSolicitud> result =
                historialRepository.findBySolicitudIdOrderByFechaAccionAsc(solicitud.getId());

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getEstadoNuevo()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(result.get(4).getEstadoNuevo()).isEqualTo(EstadoSolicitud.CERRADA);
    }
}