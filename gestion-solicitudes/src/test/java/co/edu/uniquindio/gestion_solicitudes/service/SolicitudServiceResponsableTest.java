package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.chain.CadenaValidacionFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import co.edu.uniquindio.gestion_solicitudes.domain.factory.SolicitudFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.HistorialObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.SolicitudObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.MotorReglasPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.state.SolicitudStateContext;
import co.edu.uniquindio.gestion_solicitudes.dto.request.AsignarResponsableRequest;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceResponsableTest {

    @Mock private SolicitudRepository solicitudRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialRepository historialRepository;
    @Mock private MotorReglasPrioridad motorReglasPrioridad;
    @Spy  private List<SolicitudObserver> observadores = new ArrayList<>();
    @Mock private SolicitudFactory solicitudFactory;
    @Mock private SolicitudStateContext stateContext;
    @Mock private CadenaValidacionFactory cadenaValidacionFactory;

    @InjectMocks
    private SolicitudServiceImpl solicitudService;

    private Usuario admin;
    private Usuario docente;
    private SolicitudAcademica solicitud;

    @BeforeEach
    void setUp() {
        observadores.clear();
        observadores.add(new HistorialObserver(historialRepository));
        lenient().when(solicitudFactory.toResponse(any(SolicitudAcademica.class)))
            .thenAnswer(inv -> TestDataFactory.crearResponseDesdeEntidad(inv.getArgument(0)));

        admin   = TestDataFactory.crearUsuario(1L, RolUsuario.ADMINISTRATIVO);
        docente = TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);
        solicitud = TestDataFactory.crearSolicitud(10L, TestDataFactory.crearUsuario(3L, RolUsuario.ESTUDIANTE));
    }

    @Test
    void asignarResponsable_exitoso_debeRetornarSolicitudConResponsable() {
        AsignarResponsableRequest request = new AsignarResponsableRequest();
        request.setResponsableId(2L);

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SolicitudResponse response = solicitudService.asignarResponsable(10L, request, 1L);

        assertThat(response.getResponsable()).isNotNull();
        assertThat(response.getResponsable().getId()).isEqualTo(2L);
        verify(historialRepository).save(any());
    }

    @Test
    void asignarResponsable_solicitudNoExiste_debeLanzarResourceNotFoundException() {
        AsignarResponsableRequest request = new AsignarResponsableRequest();
        request.setResponsableId(2L);
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.asignarResponsable(99L, request, 1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void asignarResponsable_responsableNoExiste_debeLanzarResourceNotFoundException() {
        AsignarResponsableRequest request = new AsignarResponsableRequest();
        request.setResponsableId(99L);

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.asignarResponsable(10L, request, 1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void asignarResponsable_responsableInactivo_debeLanzarIllegalStateException() {
        docente.setActivo(false);
        AsignarResponsableRequest request = new AsignarResponsableRequest();
        request.setResponsableId(2L);

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente));

        assertThatThrownBy(() -> solicitudService.asignarResponsable(10L, request, 1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no está activo");
    }

    @Test
    void asignarResponsable_conObservacion_debeUsarlaEnHistorial() {
        AsignarResponsableRequest request = new AsignarResponsableRequest();
        request.setResponsableId(2L);
        request.setObservacion("Asignado por prioridad académica");

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente));
        when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        solicitudService.asignarResponsable(10L, request, 1L);

        verify(historialRepository).save(argThat(h -> h.getObservaciones().equals("Asignado por prioridad académica")));
    }
}
