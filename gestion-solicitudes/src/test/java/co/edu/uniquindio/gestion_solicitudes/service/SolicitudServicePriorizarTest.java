package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.HistorialRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.SolicitudRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.UsuarioRepository;
import co.edu.uniquindio.gestion_solicitudes.service.impl.SolicitudServiceImpl;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServicePriorizarTest {

    @Mock private SolicitudRepository solicitudRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialRepository historialRepository;
    @Mock private MotorReglasPrioridad motorReglasPrioridad;

    @InjectMocks
    private SolicitudServiceImpl solicitudService;

    private Usuario admin;
    private SolicitudAcademica solicitud;

    @BeforeEach
    void setUp() {
        admin    = TestDataFactory.crearUsuario(1L, RolUsuario.ADMINISTRATIVO);
        solicitud = TestDataFactory.crearSolicitud(10L, admin);
        solicitud.setEstado(EstadoSolicitud.CLASIFICADA);
        solicitud.setImpactoAcademico(5);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(1));
    }

    @Test
    void priorizar_exitoso_debeRetornarSolicitudConPrioridad() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(motorReglasPrioridad.calcular(solicitud))
            .thenReturn(motorReglasPrioridad.calcular(solicitud));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse response = solicitudService.priorizar(10L, 1L);

        assertThat(response.getPrioridad()).isEqualTo(Prioridad.CRITICA);
        assertThat(response.getJustificacionPrioridad()).isEqualTo("Justificación de prueba.");
        verify(historialRepository).save(any());
    }

    @Test
    void priorizar_solicitudNoExiste_debeLanzarResourceNotFoundException() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.priorizar(99L, 1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void priorizar_usuarioNoExiste_debeLanzarResourceNotFoundException() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.priorizar(10L, 99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void priorizar_debeGuardarEntradaEnHistorial() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(motorReglasPrioridad.calcular(solicitud))
            .thenReturn(motorReglasPrioridad.calcular(solicitud));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        solicitudService.priorizar(10L, 1L);

        verify(historialRepository, times(1)).save(any());
    }

    @Test
    void priorizar_motorRetornaBaja_debeAsignarBaja() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(motorReglasPrioridad.calcular(solicitud))
            .thenReturn(motorReglasPrioridad.calcular(solicitud));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse response = solicitudService.priorizar(10L, 1L);

        assertThat(response.getPrioridad()).isEqualTo(Prioridad.BAJA);
    }

    //Debe lanzar IllegalStateException
    @Test
    void priorizar_solicitudNoClasificada(){
        solicitud.setEstado(EstadoSolicitud.REGISTRADA);
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> solicitudService.priorizar(10L, 1L)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLASIFICADA");

        verify(motorReglasPrioridad, never()).calcular(any());
        verify(historialRepository, never()).save(any());
    }
}
