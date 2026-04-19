package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.chain.CadenaValidacionFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.chain.ValidacionSolicitudHandler;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.domain.factory.SolicitudFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.HistorialObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.SolicitudObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.MotorReglasPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ResultadoPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.state.SolicitudStateContext;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.HistorialRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.SolicitudRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.UsuarioRepository;
import co.edu.uniquindio.gestion_solicitudes.service.impl.SolicitudServiceImpl;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - SolicitudService.priorizar")
class SolicitudServicePriorizarTest {

    @Mock private SolicitudRepository solicitudRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialRepository historialRepository;
    @Mock private MotorReglasPrioridad motorReglasPrioridad;
    @Spy  private List<SolicitudObserver> observadores = new ArrayList<>();
    @Mock private SolicitudFactory solicitudFactory;
    @Mock private SolicitudStateContext stateContext;
    @Mock private CadenaValidacionFactory cadenaValidacionFactory;

    @InjectMocks private SolicitudServiceImpl solicitudService;

    private Usuario admin;
    private SolicitudAcademica solicitud;

    @BeforeEach
    void setUp() {
        observadores.clear();
        observadores.add(new HistorialObserver(historialRepository));

        lenient().when(solicitudFactory.toResponse(any(SolicitudAcademica.class)))
                .thenAnswer(inv -> TestDataFactory.crearResponseDesdeEntidad(inv.getArgument(0)));

        ValidacionSolicitudHandler handlerNoOp = mock(ValidacionSolicitudHandler.class);
        lenient().when(cadenaValidacionFactory.construirCadenaBasica()).thenReturn(handlerNoOp);
        lenient().when(cadenaValidacionFactory.construirCadenaIniciarAtencion()).thenReturn(handlerNoOp);

        admin    = TestDataFactory.crearUsuario(1L, RolUsuario.ADMINISTRATIVO);
        solicitud = TestDataFactory.crearSolicitud(10L, admin);
        solicitud.setEstado(EstadoSolicitud.CLASIFICADA);
        solicitud.setImpactoAcademico(5);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(1));
    }

    @Test
    @DisplayName("priorizar exitoso retorna solicitud con prioridad asignada")
    void priorizar_exitoso_debeRetornarSolicitudConPrioridad() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(motorReglasPrioridad.calcular(solicitud))
                .thenReturn(new ResultadoPrioridad(Prioridad.CRITICA, "Justificación de prueba."));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse response = solicitudService.priorizar(10L, 1L);

        assertThat(response.getPrioridad()).isEqualTo(Prioridad.CRITICA);
        assertThat(response.getJustificacionPrioridad()).isEqualTo("Justificación de prueba.");
        verify(historialRepository).save(any());
    }

    @Test
    @DisplayName("priorizar con solicitud inexistente lanza ResourceNotFoundException")
    void priorizar_solicitudNoExiste_debeLanzarResourceNotFoundException() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.priorizar(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("priorizar con usuario inexistente lanza ResourceNotFoundException")
    void priorizar_usuarioNoExiste_debeLanzarResourceNotFoundException() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.priorizar(10L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("priorizar guarda entrada en el historial")
    void priorizar_debeGuardarEntradaEnHistorial() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(motorReglasPrioridad.calcular(solicitud))
                .thenReturn(new ResultadoPrioridad(Prioridad.CRITICA, "Justificación de prueba."));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        solicitudService.priorizar(10L, 1L);

        verify(historialRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("priorizar con motor que retorna BAJA asigna BAJA")
    void priorizar_motorRetornaBaja_debeAsignarBaja() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(motorReglasPrioridad.calcular(solicitud))
                .thenReturn(new ResultadoPrioridad(Prioridad.BAJA, "Prioridad baja."));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse response = solicitudService.priorizar(10L, 1L);

        assertThat(response.getPrioridad()).isEqualTo(Prioridad.BAJA);
    }

    @Test
    @DisplayName("priorizar desde REGISTRADA lanza IllegalStateException")
    void priorizar_solicitudNoClasificada_lanzaExcepcion() {
        solicitud.setEstado(EstadoSolicitud.REGISTRADA);
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> solicitudService.priorizar(10L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLASIFICADA");

        verify(motorReglasPrioridad, never()).calcular(any());
        verify(historialRepository, never()).save(any());
    }
}