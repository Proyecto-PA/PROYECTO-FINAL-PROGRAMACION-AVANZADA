package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.chain.CadenaValidacionFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.chain.ValidacionSolicitudHandler;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.domain.factory.SolicitudFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.HistorialObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.SolicitudObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.MotorReglasPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ResultadoPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.state.SolicitudStateContext;
import co.edu.uniquindio.gestion_solicitudes.domain.state.impl.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.*;
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
@DisplayName("Tests - Flujo completo ciclo de vida de solicitud")
public class FlujoCicloVidaSolicitudTest {

    @Mock private SolicitudRepository solicitudRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialRepository historialRepository;
    @Mock private MotorReglasPrioridad motorReglasPrioridad;
    @Spy  private List<SolicitudObserver> observadores = new ArrayList<>();
    @Mock private SolicitudFactory solicitudFactory;
    @Mock private SolicitudStateContext stateContext;
    @Mock private CadenaValidacionFactory cadenaValidacionFactory;

    @InjectMocks private SolicitudServiceImpl solicitudService;

    private final EstadoRegistrada  estadoRegistrada  = new EstadoRegistrada();
    private final EstadoClasificada estadoClasificada = new EstadoClasificada();
    private final EstadoEnAtencion  estadoEnAtencion  = new EstadoEnAtencion();
    private final EstadoAtendida    estadoAtendida    = new EstadoAtendida();

    @BeforeEach
    void setUp() {
        observadores.clear();
        observadores.add(new HistorialObserver(historialRepository));

        lenient().when(solicitudFactory.toResponse(any(SolicitudAcademica.class)))
                .thenAnswer(inv -> TestDataFactory.crearResponseDesdeEntidad(inv.getArgument(0)));

        ValidacionSolicitudHandler handlerNoOp = mock(ValidacionSolicitudHandler.class);
        lenient().when(cadenaValidacionFactory.construirCadenaBasica()).thenReturn(handlerNoOp);
        lenient().when(cadenaValidacionFactory.construirCadenaIniciarAtencion()).thenReturn(handlerNoOp);

        lenient().when(stateContext.resolverEstado(any(SolicitudAcademica.class)))
                .thenAnswer(inv -> {
                    SolicitudAcademica s = inv.getArgument(0);
                    return switch (s.getEstado()) {
                        case REGISTRADA  -> estadoRegistrada;
                        case CLASIFICADA -> estadoClasificada;
                        case EN_ATENCION -> estadoEnAtencion;
                        case ATENDIDA    -> estadoAtendida;
                        default -> throw new IllegalStateException(
                                "La solicitud está en estado terminal " + s.getEstado().name()
                                        + " y no puede ser modificada.");
                    };
                });
    }

    private Usuario docente() {
        return TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);
    }

    private Usuario estudiante() {
        return TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
    }

    private SolicitudAcademica solicitudEn(EstadoSolicitud estado) {
        SolicitudAcademica s = TestDataFactory.crearSolicitud(1L, estudiante());
        s.setEstado(estado);
        s.setResponsable(docente());
        return s;
    }

    private SolicitudAcademica solicitudGuardadaEn(EstadoSolicitud estado) {
        SolicitudAcademica s = TestDataFactory.crearSolicitud(1L, estudiante());
        s.setEstado(estado);
        s.setResponsable(docente());
        return s;
    }

    private CambiarEstadoRequest obs(String texto) {
        CambiarEstadoRequest r = new CambiarEstadoRequest();
        r.setObservacion(texto);
        return r;
    }

    private CerrarSolicitudRequest obsCierre(String texto) {
        CerrarSolicitudRequest r = new CerrarSolicitudRequest();
        r.setObservacion(texto);
        return r;
    }

    // ---- FLUJO COMPLETO ----

    @Test
    @DisplayName("Flujo completo REGISTRADA → CLASIFICADA → EN_ATENCION → ATENDIDA → CERRADA")
    void flujoCompleto_cicloDeVidaCompleto() {
        SolicitudAcademica enRegistrada = solicitudEn(EstadoSolicitud.REGISTRADA);
        SolicitudAcademica enClasificada = solicitudGuardadaEn(EstadoSolicitud.CLASIFICADA);
        enClasificada.setTipo(TipoSolicitud.HOMOLOGACION);
        enClasificada.setPrioridad(Prioridad.MEDIA);
        enClasificada.setJustificacionPrioridad("Prioridad asignada automáticamente");

        when(solicitudRepository.findById(1L))
                .thenReturn(Optional.of(enRegistrada))
                .thenReturn(Optional.of(enClasificada))
                .thenReturn(Optional.of(solicitudGuardadaEn(EstadoSolicitud.EN_ATENCION)))
                .thenReturn(Optional.of(solicitudGuardadaEn(EstadoSolicitud.ATENDIDA)));

        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(docente()));

        when(solicitudRepository.save(any()))
                .thenReturn(enClasificada)
                .thenReturn(solicitudGuardadaEn(EstadoSolicitud.EN_ATENCION))
                .thenReturn(solicitudGuardadaEn(EstadoSolicitud.ATENDIDA))
                .thenReturn(solicitudGuardadaEn(EstadoSolicitud.CERRADA));

        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());
        when(motorReglasPrioridad.calcular(any()))
                .thenReturn(new ResultadoPrioridad(Prioridad.MEDIA, "Justificación de prueba"));

        ClasificarRequest clasificarReq = new ClasificarRequest();
        clasificarReq.setTipo(TipoSolicitud.HOMOLOGACION);
        clasificarReq.setImpactoAcademico(3);
        clasificarReq.setObservacion("Clasificada como homologación");

        SolicitudResponse r1 = solicitudService.clasificar(1L, clasificarReq, 2L);
        assertThat(r1.getEstado()).isEqualTo(EstadoSolicitud.CLASIFICADA);

        SolicitudResponse r2 = solicitudService.iniciarAtencion(1L, obs("Iniciando"), 2L);
        assertThat(r2.getEstado()).isEqualTo(EstadoSolicitud.EN_ATENCION);

        SolicitudResponse r3 = solicitudService.marcarAtendida(1L, obs("Resuelto"), 2L);
        assertThat(r3.getEstado()).isEqualTo(EstadoSolicitud.ATENDIDA);

        SolicitudResponse r4 = solicitudService.cerrar(1L, obsCierre("Solicitud cerrada formalmente."), 2L);
        assertThat(r4.getEstado()).isEqualTo(EstadoSolicitud.CERRADA);

        verify(historialRepository, times(5)).save(any(HistorialSolicitud.class));
        verify(solicitudRepository, times(4)).save(any(SolicitudAcademica.class));
    }

    @Test
    @DisplayName("Flujo cancelación: REGISTRADA → CANCELADA")
    void flujoCancelacion_desdeRegistrada() {
        SolicitudAcademica enRegistrada = solicitudEn(EstadoSolicitud.REGISTRADA);
        SolicitudAcademica cancelada    = solicitudGuardadaEn(EstadoSolicitud.CANCELADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(enRegistrada));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante()));
        when(solicitudRepository.save(any())).thenReturn(cancelada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        SolicitudResponse response = solicitudService.cancelar(1L, obs("Cancelada por error"), 1L);

        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CANCELADA);
        verify(historialRepository, times(1)).save(any(HistorialSolicitud.class));
    }

    @Test
    @DisplayName("No se puede cancelar desde CLASIFICADA")
    void flujo_noCancelarDesdeClasificada() {
        SolicitudAcademica enClasificada = solicitudEn(EstadoSolicitud.CLASIFICADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(enClasificada));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(estudiante()));

        assertThatThrownBy(() -> solicitudService.cancelar(1L, obs("Quiero cancelar"), 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REGISTRADA");

        verify(solicitudRepository, never()).save(any());
        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("No se puede retroceder de EN_ATENCION a CLASIFICADA")
    void flujo_noRetrocederDeEnAtencion() {
        SolicitudAcademica enAtencion = solicitudEn(EstadoSolicitud.EN_ATENCION);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(enAtencion));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));

        ClasificarRequest req = new ClasificarRequest();
        req.setTipo(TipoSolicitud.OTRO);
        req.setImpactoAcademico(2);

        assertThatThrownBy(() -> solicitudService.clasificar(1L, req, 2L))
                .isInstanceOf(IllegalStateException.class);

        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("No se puede saltar de REGISTRADA a EN_ATENCION")
    void flujo_noSaltarEstados_registradaAEnAtencion() {
        SolicitudAcademica enRegistrada = solicitudEn(EstadoSolicitud.REGISTRADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(enRegistrada));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));

        assertThatThrownBy(() -> solicitudService.iniciarAtencion(1L, obs("Saltando"), 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REGISTRADA");

        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("No se puede saltar de REGISTRADA a ATENDIDA")
    void flujo_noSaltarEstados_registradaAAtendida() {
        SolicitudAcademica enRegistrada = solicitudEn(EstadoSolicitud.REGISTRADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(enRegistrada));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));

        assertThatThrownBy(() -> solicitudService.marcarAtendida(1L, obs("Saltando"), 2L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No se puede saltar de CLASIFICADA a CERRADA")
    void flujo_noSaltarEstados_clasificadaACerrada() {
        SolicitudAcademica enClasificada = solicitudEn(EstadoSolicitud.CLASIFICADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(enClasificada));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));

        assertThatThrownBy(() -> solicitudService.cerrar(1L, obsCierre("Cerrando directamente"), 2L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Estado CERRADA es terminal: no acepta ninguna transición")
    void flujo_cerradaEsTerminal() {
        SolicitudAcademica cerrada = solicitudEn(EstadoSolicitud.CERRADA);

        Usuario docente = TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(cerrada));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente)); // 🔥 FALTABA ESTO

        assertThatThrownBy(() -> solicitudService.iniciarAtencion(1L, obs("Reactivando"), 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CERRADA");

        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Estado CANCELADA es terminal: no acepta ninguna transición")
    void flujo_canceladaEsTerminal() {
        SolicitudAcademica cancelada = solicitudEn(EstadoSolicitud.CANCELADA);

        Usuario docente = TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(cancelada));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente)); // 🔥 FALTABA ESTO

        ClasificarRequest req = new ClasificarRequest();
        req.setTipo(TipoSolicitud.OTRO);
        req.setImpactoAcademico(2);

        assertThatThrownBy(() -> solicitudService.clasificar(1L, req, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CANCELADA");

        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cada transición registra exactamente una entrada en historial")
    void flujo_cadaTransicionRegistraHistorial() {
        SolicitudAcademica enClasificada = solicitudEn(EstadoSolicitud.CLASIFICADA);
        SolicitudAcademica enAtencion    = solicitudGuardadaEn(EstadoSolicitud.EN_ATENCION);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(enClasificada));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));
        when(solicitudRepository.save(any())).thenReturn(enAtencion);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        solicitudService.iniciarAtencion(1L, obs("Una sola entrada"), 2L);

        verify(historialRepository, times(1)).save(any(HistorialSolicitud.class));
    }

    @Test
    @DisplayName("Cerrar registra la observación en el historial")
    void flujo_cerrar_observacionGuardadaEnHistorial() {
        SolicitudAcademica enAtendida = solicitudEn(EstadoSolicitud.ATENDIDA);
        SolicitudAcademica cerrada    = solicitudGuardadaEn(EstadoSolicitud.CERRADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(enAtendida));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente()));
        when(solicitudRepository.save(any())).thenReturn(cerrada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());

        CerrarSolicitudRequest req = new CerrarSolicitudRequest();
        req.setObservacion("Cierre formal con documentación completa adjunta.");

        SolicitudResponse response = solicitudService.cerrar(1L, req, 2L);

        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CERRADA);

        ArgumentCaptor<HistorialSolicitud> captor = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(captor.capture());
        assertThat(captor.getValue().getObservaciones())
                .isEqualTo("Cierre formal con documentación completa adjunta.");
    }

    @Test
    @DisplayName("Usuario inexistente en cualquier transición lanza ResourceNotFoundException")
    void flujo_usuarioInexistente_lanzaExcepcion() {
        SolicitudAcademica enClasificada = solicitudEn(EstadoSolicitud.CLASIFICADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(enClasificada));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.iniciarAtencion(1L, obs("obs"), 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(historialRepository, never()).save(any());
    }
}