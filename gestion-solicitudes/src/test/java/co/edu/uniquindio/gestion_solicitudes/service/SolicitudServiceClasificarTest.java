package co.edu.uniquindio.gestion_solicitudes.service;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.HistorialSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.HistorialRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.SolicitudRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.UsuarioRepository;
import co.edu.uniquindio.gestion_solicitudes.service.impl.SolicitudServiceImpl;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - SolicitudService.clasificar")

public class SolicitudServiceClasificarTest {

    @Mock private SolicitudRepository solicitudRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HistorialRepository historialRepository;
    @Mock private MotorReglasPrioridad motorReglasPrioridad;

    @InjectMocks private SolicitudServiceImpl solicitudService;

    private ClasificarRequest crearRequest(TipoSolicitud tipo, Integer impacto, String obs) {
        ClasificarRequest r = new ClasificarRequest();
        r.setTipo(tipo);
        r.setImpactoAcademico(impacto);
        r.setObservacion(obs);
        return r;
    }

    @Test
    @DisplayName("clasificar - estado REGISTRADA cambia a CLASIFICADA exitosamente")
    void clasificar_estadoRegistrada_cambiaAClasificada() {
        Usuario docente = TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);
        // estado inicial = REGISTRADA (viene del factory)

        SolicitudAcademica clasificada = TestDataFactory.crearSolicitud(1L, solicitante);
        clasificada.setEstado(EstadoSolicitud.CLASIFICADA);
        clasificada.setTipo(TipoSolicitud.HOMOLOGACION);
        clasificada.setImpactoAcademico(3);
        clasificada.setPrioridad(Prioridad.MEDIA);
        clasificada.setJustificacionPrioridad("Test justificación");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente));
        when(solicitudRepository.save(any())).thenReturn(clasificada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());
        when(motorReglasPrioridad.calcular(any())).thenReturn(
            motorReglasPrioridad.calcular(solicitud));

        SolicitudResponse response = solicitudService.clasificar(
            1L, crearRequest(TipoSolicitud.HOMOLOGACION, 3, "Clasificada manualmente"), 2L);

        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CLASIFICADA);
        assertThat(response.getTipo()).isEqualTo(TipoSolicitud.HOMOLOGACION);
        // Se guardan dos registros en historial: uno para cambio de estado, otro para prioridad
        verify(historialRepository, times(2)).save(any(HistorialSolicitud.class));
        verify(solicitudRepository).save(any(SolicitudAcademica.class));
    }

    @Test
    @DisplayName("clasificar - solicitud inexistente lanza ResourceNotFoundException")
    void clasificar_solicitudInexistente_lanzaExcepcion() {
        when(solicitudRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            solicitudService.clasificar(999L, crearRequest(TipoSolicitud.HOMOLOGACION, 3, null), 1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");

        verify(solicitudRepository, never()).save(any());
        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("clasificar - estado CLASIFICADA lanza IllegalStateException (transición inválida)")
    void clasificar_estadoYaClasificado_lanzaExcepcion() {
        Usuario docente = TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);
        solicitud.setEstado(EstadoSolicitud.CLASIFICADA); // ya fue clasificada

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente));

        assertThatThrownBy(() ->
            solicitudService.clasificar(1L, crearRequest(TipoSolicitud.HOMOLOGACION, 3, null), 2L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No es posible pasar de CLASIFICADA a CLASIFICADA");

        verify(solicitudRepository, never()).save(any());
        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("clasificar - estado EN_ATENCION lanza IllegalStateException")
    void clasificar_estadoEnAtencion_lanzaExcepcion() {
        Usuario docente = TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);
        solicitud.setEstado(EstadoSolicitud.EN_ATENCION);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente));

        assertThatThrownBy(() ->
            solicitudService.clasificar(1L, crearRequest(TipoSolicitud.CONSULTA_ACADEMICA, 2, null), 2L))
            .isInstanceOf(IllegalStateException.class);

        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("clasificar - estado CANCELADA lanza IllegalStateException")
    void clasificar_estadoCancelada_lanzaExcepcion() {
        Usuario docente = TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);
        solicitud.setEstado(EstadoSolicitud.CANCELADA);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente));

        assertThatThrownBy(() ->
            solicitudService.clasificar(1L, crearRequest(TipoSolicitud.OTRO, 4, null), 2L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("clasificar - usuario clasificador inexistente lanza ResourceNotFoundException")
    void clasificar_usuarioInexistente_lanzaExcepcion() {
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            solicitudService.clasificar(1L, crearRequest(TipoSolicitud.HOMOLOGACION, 3, null), 999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");

        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("clasificar - sin observación usa texto por defecto en historial")
    void clasificar_sinObservacion_usaTextoDefault() {
        Usuario docente = TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);
        SolicitudAcademica clasificada = TestDataFactory.crearSolicitud(1L, solicitante);
        clasificada.setEstado(EstadoSolicitud.CLASIFICADA);
        clasificada.setTipo(TipoSolicitud.CANCELACION_ASIGNATURAS);
        clasificada.setImpactoAcademico(4);
        clasificada.setPrioridad(Prioridad.ALTA);
        clasificada.setJustificacionPrioridad("Test justificación");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente));
        when(solicitudRepository.save(any())).thenReturn(clasificada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());
        when(motorReglasPrioridad.calcular(any())).thenReturn(
            motorReglasPrioridad.calcular(solicitud));

        // observacion = null → debe usar texto por defecto
        SolicitudResponse response = solicitudService.clasificar(
            1L, crearRequest(TipoSolicitud.CANCELACION_ASIGNATURAS, 4, null), 2L);

        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CLASIFICADA);
        // Verificamos que historial SÍ se guarda dos veces (cambio de estado y prioridad)
        ArgumentCaptor<HistorialSolicitud> captor =
            ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository, times(2)).save(captor.capture());
        // El primer registro debe contener la información de clasificación
        assertThat(captor.getAllValues().get(0).getObservaciones())
            .contains("CANCELACION_ASIGNATURAS");
    }

    @Test
    @DisplayName("clasificar - historial registra estados anterior y nuevo correctamente")
    void clasificar_historialRegistraEstadosCorrectamente() {
        Usuario docente = TestDataFactory.crearUsuario(2L, RolUsuario.DOCENTE);
        Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);
        SolicitudAcademica clasificada = TestDataFactory.crearSolicitud(1L, solicitante);
        clasificada.setEstado(EstadoSolicitud.CLASIFICADA);
        clasificada.setTipo(TipoSolicitud.SOLICITUD_CUPOS);
        clasificada.setImpactoAcademico(2);
        clasificada.setPrioridad(Prioridad.BAJA);
        clasificada.setJustificacionPrioridad("Test justificación");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(docente));
        when(solicitudRepository.save(any())).thenReturn(clasificada);
        when(historialRepository.save(any())).thenReturn(new HistorialSolicitud());
        when(motorReglasPrioridad.calcular(any())).thenReturn(
            motorReglasPrioridad.calcular(solicitud));

        solicitudService.clasificar(1L, crearRequest(TipoSolicitud.SOLICITUD_CUPOS, 2, "obs"), 2L);

        ArgumentCaptor<HistorialSolicitud> captor =
            ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository, times(2)).save(captor.capture());

        // El primer registro corresponde al cambio de estado
        HistorialSolicitud registrado = captor.getAllValues().get(0);
        assertThat(registrado.getEstadoAnterior()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(registrado.getEstadoNuevo()).isEqualTo(EstadoSolicitud.CLASIFICADA);
        assertThat(registrado.getUsuarioResponsable().getId()).isEqualTo(2L);
    }
    
}
