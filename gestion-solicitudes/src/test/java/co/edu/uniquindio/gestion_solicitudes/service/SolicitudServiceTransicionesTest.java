package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.chain.CadenaValidacionFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.domain.factory.SolicitudFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.HistorialObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.SolicitudObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.MotorReglasPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.state.SolicitudStateContext;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CambiarEstadoRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CerrarSolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.*;
import co.edu.uniquindio.gestion_solicitudes.service.impl.SolicitudServiceImpl;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - SolicitudService transiciones de estado")
public class SolicitudServiceTransicionesTest {

    @Mock private SolicitudRepository solicitudRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialRepository historialRepository;
    @Mock private MotorReglasPrioridad motorReglasPrioridad;
    @Spy  private List<SolicitudObserver> observadores = new ArrayList<>();
    @Mock private SolicitudFactory solicitudFactory;
    @Mock private SolicitudStateContext stateContext;
    @Mock private CadenaValidacionFactory cadenaValidacionFactory;

    @InjectMocks private SolicitudServiceImpl solicitudService;

    @BeforeEach
    void setUp() {
        observadores.clear();
        observadores.add(new HistorialObserver(historialRepository));
        lenient().when(solicitudFactory.toResponse(any(SolicitudAcademica.class)))
            .thenAnswer(inv -> TestDataFactory.crearResponseDesdeEntidad(inv.getArgument(0)));
    }

    private Usuario docente() { return TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE); }
    private Usuario estudiante() { return TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE); }

    private SolicitudAcademica solicitudEn(EstadoSolicitud estado) {
        SolicitudAcademica s = TestDataFactory.crearSolicitud(1L, estudiante());
        s.setEstado(estado);
        return s;
    }

    private SolicitudAcademica solicitudGuardadaEn(EstadoSolicitud estado) {
        SolicitudAcademica s = TestDataFactory.crearSolicitud(1L, estudiante());
        s.setEstado(estado);
        return s;
    }

    private CambiarEstadoRequest requestConObs(String obs) {
        CambiarEstadoRequest r = new CambiarEstadoRequest();
        r.setObservacion(obs);
        return r;
    }

    private CerrarSolicitudRequest requestCierre(String obs) {
        CerrarSolicitudRequest r = new CerrarSolicitudRequest();
        r.setObservacion(obs);
        return r;
    }

    // ── INICIAR ATENCIÓN ─────────────────────────────────────

    @Test
    @DisplayName("iniciarAtencion - CLASIFICADA → EN_ATENCION exitosamente")
    void iniciarAtencion_desdeClasificada_cambiaEstado() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.CLASIFICADA);
        SolicitudAcademica guardada = solicitudGuardadaEn(EstadoSolicitud.EN_ATENCION);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));
        when(solicitudRepository.save(any())).thenReturn(guardada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        SolicitudResponse response = solicitudService.iniciarAtencion(
            1L, requestConObs("Iniciando revisión"), 2L);

        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.EN_ATENCION);
        verify(historialRepository).save(any(HistorialSolicitud.class));
        verify(solicitudRepository).save(any(SolicitudAcademica.class));
    }

    @Test
    @DisplayName("iniciarAtencion - desde REGISTRADA lanza IllegalStateException")
    void iniciarAtencion_desdeRegistrada_lanzaExcepcion() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.REGISTRADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));

        assertThatThrownBy(() ->
            solicitudService.iniciarAtencion(1L, requestConObs("obs"), 2L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("REGISTRADA")
            .hasMessageContaining("EN_ATENCION");

        verify(solicitudRepository, never()).save(any());
        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("iniciarAtencion - solicitud inexistente lanza ResourceNotFoundException")
    void iniciarAtencion_solicitudInexistente_lanzaExcepcion() {
        when(solicitudRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            solicitudService.iniciarAtencion(999L, requestConObs("obs"), 2L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    @DisplayName("iniciarAtencion - historial registra estados correctamente")
    void iniciarAtencion_historialRegistraEstados() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.CLASIFICADA);
        SolicitudAcademica guardada = solicitudGuardadaEn(EstadoSolicitud.EN_ATENCION);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));
        when(solicitudRepository.save(any())).thenReturn(guardada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        solicitudService.iniciarAtencion(1L, requestConObs("obs"), 2L);

        ArgumentCaptor<HistorialSolicitud> captor =
            ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(captor.capture());

        assertThat(captor.getValue().getEstadoAnterior()).isEqualTo(EstadoSolicitud.CLASIFICADA);
        assertThat(captor.getValue().getEstadoNuevo()).isEqualTo(EstadoSolicitud.EN_ATENCION);
    }

    @Test
    @DisplayName("iniciarAtencion - sin observacion usa texto por defecto")
    void iniciarAtencion_sinObservacion_usaDefault() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.CLASIFICADA);
        SolicitudAcademica guardada = solicitudGuardadaEn(EstadoSolicitud.EN_ATENCION);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));
        when(solicitudRepository.save(any())).thenReturn(guardada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        solicitudService.iniciarAtencion(1L, null, 2L);

        ArgumentCaptor<HistorialSolicitud> captor =
            ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(captor.capture());
        assertThat(captor.getValue().getObservaciones()).isNotBlank();
    }

    // ── MARCAR ATENDIDA ──────────────────────────────────────

    @Test
    @DisplayName("marcarAtendida - EN_ATENCION → ATENDIDA exitosamente")
    void marcarAtendida_desdeEnAtencion_cambiaEstado() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.EN_ATENCION);
        SolicitudAcademica guardada = solicitudGuardadaEn(EstadoSolicitud.ATENDIDA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));
        when(solicitudRepository.save(any())).thenReturn(guardada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        SolicitudResponse response = solicitudService.marcarAtendida(
            1L, requestConObs("Trámite resuelto"), 2L);

        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.ATENDIDA);
        verify(historialRepository).save(any(HistorialSolicitud.class));
    }

    @Test
    @DisplayName("marcarAtendida - desde CLASIFICADA lanza IllegalStateException")
    void marcarAtendida_desdeClasificada_lanzaExcepcion() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.CLASIFICADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));

        assertThatThrownBy(() ->
            solicitudService.marcarAtendida(1L, requestConObs("obs"), 2L))
            .isInstanceOf(IllegalStateException.class);

        verify(solicitudRepository, never()).save(any());
        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("marcarAtendida - historial registra estados correctamente")
    void marcarAtendida_historialRegistraEstados() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.EN_ATENCION);
        SolicitudAcademica guardada = solicitudGuardadaEn(EstadoSolicitud.ATENDIDA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));
        when(solicitudRepository.save(any())).thenReturn(guardada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        solicitudService.marcarAtendida(1L, requestConObs("obs"), 2L);

        ArgumentCaptor<HistorialSolicitud> captor =
            ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(captor.capture());

        assertThat(captor.getValue().getEstadoAnterior()).isEqualTo(EstadoSolicitud.EN_ATENCION);
        assertThat(captor.getValue().getEstadoNuevo()).isEqualTo(EstadoSolicitud.ATENDIDA);
    }

    // ── CERRAR ───────────────────────────────────────────────

    @Test
    @DisplayName("cerrar - ATENDIDA → CERRADA exitosamente")
    void cerrar_desdeAtendida_cambiaEstado() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.ATENDIDA);
        SolicitudAcademica guardada = solicitudGuardadaEn(EstadoSolicitud.CERRADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));
        when(solicitudRepository.save(any())).thenReturn(guardada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        SolicitudResponse response = solicitudService.cerrar(
            1L, requestCierre("Solicitud resuelta exitosamente."), 2L);

        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CERRADA);
        verify(historialRepository).save(any(HistorialSolicitud.class));
    }

    @Test
    @DisplayName("cerrar - desde EN_ATENCION lanza IllegalStateException")
    void cerrar_desdeEnAtencion_lanzaExcepcion() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.EN_ATENCION);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));

        assertThatThrownBy(() ->
            solicitudService.cerrar(1L, requestCierre("Cierre"), 2L))
            .isInstanceOf(IllegalStateException.class);

        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("cerrar - solicitud ya CERRADA lanza IllegalStateException")
    void cerrar_solicitudYaCerrada_lanzaExcepcion() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.CERRADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() ->
            solicitudService.cerrar(1L, requestCierre("Cierre duplicado"), 2L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cerrar - historial registra observacion de cierre")
    void cerrar_historialRegistraObservacion() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.ATENDIDA);
        SolicitudAcademica guardada = solicitudGuardadaEn(EstadoSolicitud.CERRADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));
        when(solicitudRepository.save(any())).thenReturn(guardada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        solicitudService.cerrar(1L, requestCierre("Resolución formal del caso"), 2L);

        ArgumentCaptor<HistorialSolicitud> captor =
            ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(captor.capture());

        assertThat(captor.getValue().getObservaciones()).isEqualTo("Resolución formal del caso");
        assertThat(captor.getValue().getEstadoNuevo()).isEqualTo(EstadoSolicitud.CERRADA);
    }

    // ── CANCELAR ─────────────────────────────────────────────

    @Test
    @DisplayName("cancelar - REGISTRADA → CANCELADA exitosamente")
    void cancelar_desdeRegistrada_cambiaEstado() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.REGISTRADA);
        SolicitudAcademica guardada = solicitudGuardadaEn(EstadoSolicitud.CANCELADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante()));
        when(solicitudRepository.save(any())).thenReturn(guardada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        SolicitudResponse response = solicitudService.cancelar(
            1L, requestConObs("Cancelada por error"), 1L);

        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CANCELADA);
        verify(historialRepository).save(any(HistorialSolicitud.class));
    }

    @Test
    @DisplayName("cancelar - desde CLASIFICADA lanza IllegalStateException")
    void cancelar_desdeClasificada_lanzaExcepcion() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.CLASIFICADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante()));

        assertThatThrownBy(() ->
            solicitudService.cancelar(1L, requestConObs("obs"), 1L))
            .isInstanceOf(IllegalStateException.class);

        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelar - solicitud ya CANCELADA lanza IllegalStateException")
    void cancelar_solicitudYaCancelada_lanzaExcepcion() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.CANCELADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() ->
            solicitudService.cancelar(1L, requestConObs("doble cancelación"), 1L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cancelar - historial registra estados correctamente")
    void cancelar_historialRegistraEstados() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.REGISTRADA);
        SolicitudAcademica guardada = solicitudGuardadaEn(EstadoSolicitud.CANCELADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante()));
        when(solicitudRepository.save(any())).thenReturn(guardada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        solicitudService.cancelar(1L, requestConObs("Cancelada"), 1L);

        ArgumentCaptor<HistorialSolicitud> captor =
            ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(captor.capture());

        assertThat(captor.getValue().getEstadoAnterior()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(captor.getValue().getEstadoNuevo()).isEqualTo(EstadoSolicitud.CANCELADA);
    }

    // ── FLUJO COMPLETO ───────────────────────────────────────

    @Test
    @DisplayName("flujo completo - no se puede retroceder de EN_ATENCION a REGISTRADA")
    void flujo_noSePuedeRetroceder() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.EN_ATENCION);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));

        co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest clasificarReq =
            new co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest();
        clasificarReq.setTipo(TipoSolicitud.OTRO);

        assertThatThrownBy(() ->
            solicitudService.clasificar(1L, clasificarReq, 2L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("flujo completo - no se puede saltar de REGISTRADA a ATENDIDA")
    void flujo_noSePuedeSaltarEstados() {
        SolicitudAcademica solicitud = solicitudEn(EstadoSolicitud.REGISTRADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));

        assertThatThrownBy(() ->
            solicitudService.marcarAtendida(1L, requestConObs("obs"), 2L))
            .isInstanceOf(IllegalStateException.class);
    }
}
