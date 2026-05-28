package co.edu.uniquindio.gestion_solicitudes.service.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.chain.CadenaValidacionFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.domain.factory.SolicitudFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.SolicitudObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ResultadoPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.state.SolicitudStateContext;
import co.edu.uniquindio.gestion_solicitudes.dto.request.AsignarResponsableRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.SolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.*;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.*;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.MotorReglasPrioridad;
import co.edu.uniquindio.gestion_solicitudes.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CambiarEstadoRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CerrarSolicitudRequest;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    // ---- Repositorios ----
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialRepository historialRepository;

    // ---- Patrones ----
    private final SolicitudFactory solicitudFactory;
    private final SolicitudStateContext stateContext;
    private final CadenaValidacionFactory cadenaValidacionFactory;
    private final MotorReglasPrioridad motorReglasPrioridad;
    private final List<SolicitudObserver> observadores;

    // ---- Constantes de dominio ----
    private static final Set<RolUsuario> ROLES_RESPONSABLE_VALIDOS =
            Set.of(RolUsuario.DOCENTE, RolUsuario.ADMINISTRATIVO);

    // --- HELPERS - notificación a observers ---

    private void notificarCambioEstado(SolicitudAcademica solicitud, EstadoSolicitud estadoAnterior,
            EstadoSolicitud estadoNuevo, Usuario responsable, String observacion) {
        for (SolicitudObserver obs : observadores) {
            obs.onCambioEstado(solicitud, estadoAnterior, estadoNuevo, responsable, observacion);
        }
    }

    private void notificarAsignacionResponsable(SolicitudAcademica solicitud, Usuario responsable,
            Usuario ejecutor, String observacion) {
        for (SolicitudObserver obs : observadores) {
            obs.onAsignacionResponsable(solicitud, responsable, ejecutor, observacion);
        }
    }

    private void notificarPrioridadCalculada(SolicitudAcademica solicitud, Usuario ejecutor,
            String justificacion) {
        for (SolicitudObserver obs : observadores) {
            obs.onPrioridadCalculada(solicitud, ejecutor, justificacion);
        }
    }

    // ---- HELPERS - Lookups comunes ---

    private SolicitudAcademica buscarSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una solicitud con id " + id));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con id " + id));
    }

    // ====================
    // RF-01 - Registrar Solicitud
    // ====================

    @Override
    @Transactional
    public SolicitudResponse registrar(SolicitudRequest request, Long solicitanteId) {
        Usuario solicitante = buscarUsuario(solicitanteId);

        SolicitudAcademica solicitud = solicitudFactory.crearDesdeRequest(request, solicitante);
        solicitud = solicitudRepository.save(solicitud);

        historialRepository.save(HistorialSolicitud.builder()
                .solicitud(solicitud)
                .fechaAccion(LocalDateTime.now())
                .accionRealizada("Solicitud registrada en el sistema")
                .usuarioResponsable(solicitante)
                .estadoAnterior(null)
                .estadoNuevo(EstadoSolicitud.REGISTRADA)
                .build());

        return solicitudFactory.toResponse(solicitud);
    }

    // ======================
    // RF-07 - Consultar solicitudes
    // ======================

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponse obtenerPorId(Long id) {
        return solicitudFactory.toResponse(buscarSolicitud(id));
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudPageResponse consultar(EstadoSolicitud estado, TipoSolicitud tipo, Prioridad prioridad,
            Long responsableId, Long solicitanteId, Boolean sinResponsable,
            Long userId, String rol, Pageable pageable) {

        Long solicitanteIdFinal = "ESTUDIANTE".equals(rol) ? userId : solicitanteId;
        Long responsableIdFinal = "DOCENTE".equals(rol) ? userId : responsableId;

        Page<SolicitudAcademica> page = solicitudRepository.findWithFilters(
                estado, tipo, prioridad, responsableIdFinal, solicitanteIdFinal,
                sinResponsable, pageable);

        return SolicitudPageResponse.builder()
                .contenido(page.getContent().stream().map(solicitudFactory::toResponse).toList())
                .paginaActual(page.getNumber())
                .totalPaginas(page.getTotalPages())
                .totalElementos(page.getTotalElements())
                .build();
    }

    // ======================================
    // RF-02 - Clasificar (REGISTRADA a CLASIFICADA)
    // ======================================

    @Override
    @Transactional
    public SolicitudResponse clasificar(Long id, ClasificarRequest request, Long usuarioId) {
        SolicitudAcademica solicitud = buscarSolicitud(id);
        Usuario usuario = buscarUsuario(usuarioId);

        cadenaValidacionFactory.construirCadenaBasica().validar(solicitud, usuario);
        EstadoSolicitud estadoAnterior = solicitud.getEstado();

        stateContext.resolverEstado(solicitud).clasificar(solicitud, usuario, request);

        ResultadoPrioridad resultado = motorReglasPrioridad.calcular(solicitud);
        solicitud.setPrioridad(resultado.getPrioridad());
        solicitud.setJustificacionPrioridad(resultado.getJustificacion());

        solicitud = solicitudRepository.save(solicitud);

        String observacion = request.getObservacion() != null
                ? request.getObservacion()
                : "Clasificada como " + request.getTipo().name() + " con impacto académico " + request.getImpactoAcademico();

        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.CLASIFICADA, usuario, observacion);
        notificarPrioridadCalculada(solicitud, usuario, resultado.getJustificacion());

        return solicitudFactory.toResponse(solicitud);
    }

    // ============================================
    // RF-04 - Iniciar atención (CLASIFICADA a EN_ATENCION)
    // ============================================

    @Override
    @Transactional
    public SolicitudResponse iniciarAtencion(Long id, CambiarEstadoRequest request, Long usuarioId) {
        SolicitudAcademica solicitud = buscarSolicitud(id);
        Usuario usuario = buscarUsuario(usuarioId);

        cadenaValidacionFactory.construirCadenaIniciarAtencion().validar(solicitud, usuario);
        EstadoSolicitud estadoAnterior = solicitud.getEstado();

        stateContext.resolverEstado(solicitud).iniciarAtencion(solicitud, usuario,
                request != null ? request.getObservacion() : null);

        solicitud = solicitudRepository.save(solicitud);

        String observacion = (request != null && request.getObservacion() != null)
                ? request.getObservacion()
                : "Se inició la atención de la solicitud";

        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.EN_ATENCION, usuario, observacion);

        return solicitudFactory.toResponse(solicitud);
    }

    // ============================================
    // RF-04 - Marcar atendida (EN_ATENCION a ATENDIDA)
    // ============================================

    @Override
    @Transactional
    public SolicitudResponse marcarAtendida(Long id, CambiarEstadoRequest request, Long usuarioId) {
        SolicitudAcademica solicitud = buscarSolicitud(id);
        Usuario usuario = buscarUsuario(usuarioId);

        cadenaValidacionFactory.construirCadenaBasica().validar(solicitud, usuario);
        EstadoSolicitud estadoAnterior = solicitud.getEstado();

        stateContext.resolverEstado(solicitud).marcarAtendida(solicitud, usuario,
                request != null ? request.getObservacion() : null);

        solicitud = solicitudRepository.save(solicitud);

        String observacion = (request != null && request.getObservacion() != null)
                ? request.getObservacion()
                : "Solicitud marcada como atendida.";

        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.ATENDIDA, usuario, observacion);

        return solicitudFactory.toResponse(solicitud);
    }

    // ============================================
    // Cerrar (ATENDIDA a CERRADA)
    // ============================================

    @Override
    @Transactional
    public SolicitudResponse cerrar(Long id, CerrarSolicitudRequest request, Long usuarioId) {
        SolicitudAcademica solicitud = buscarSolicitud(id);
        Usuario usuario = buscarUsuario(usuarioId);

        cadenaValidacionFactory.construirCadenaBasica().validar(solicitud, usuario);
        EstadoSolicitud estadoAnterior = solicitud.getEstado();

        stateContext.resolverEstado(solicitud).cerrar(solicitud, usuario, request.getObservacion());

        solicitud = solicitudRepository.save(solicitud);

        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.CERRADA, usuario, request.getObservacion());

        return solicitudFactory.toResponse(solicitud);
    }

    // ============================================
    // Cancelar (REGISTRADA a CANCELADA)
    // ============================================

    @Override
    @Transactional
    public SolicitudResponse cancelar(Long id, CambiarEstadoRequest request, Long usuarioId) {
        SolicitudAcademica solicitud = buscarSolicitud(id);
        Usuario usuario = buscarUsuario(usuarioId);

        cadenaValidacionFactory.construirCadenaBasica().validar(solicitud, usuario);

        if (usuario.getRol() == RolUsuario.ESTUDIANTE && !solicitud.getSolicitante().getId().equals(usuarioId)) {
            throw new IllegalStateException("Un estudiante solo puede cancelar sus propias solicitudes");
        }

        EstadoSolicitud estadoAnterior = solicitud.getEstado();

        stateContext.resolverEstado(solicitud).cancelar(solicitud, usuario,
                request != null ? request.getObservacion() : null);

        solicitud = solicitudRepository.save(solicitud);

        String observacion = (request != null && request.getObservacion() != null)
                ? request.getObservacion()
                : "Solicitud cancelada";

        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.CANCELADA, usuario, observacion);

        return solicitudFactory.toResponse(solicitud);
    }

    // ============
    // RF-03 - Priorizar
    // ============

    @Override
    @Transactional
    public SolicitudResponse priorizar(Long id, Long usuarioId) {
        SolicitudAcademica solicitud = buscarSolicitud(id);
        Usuario usuario = buscarUsuario(usuarioId);

        cadenaValidacionFactory.construirCadenaBasica().validar(solicitud, usuario);

        if (solicitud.getEstado() != EstadoSolicitud.CLASIFICADA) {
            throw new IllegalStateException("Solo se puede priorizar una solicitud en estado CLASIFICADA. "
                    + "Estado actual: " + solicitud.getEstado());
        }

        ResultadoPrioridad resultado = motorReglasPrioridad.calcular(solicitud);
        solicitud.setPrioridad(resultado.getPrioridad());
        solicitud.setJustificacionPrioridad(resultado.getJustificacion());
        solicitud = solicitudRepository.save(solicitud);

        notificarPrioridadCalculada(solicitud, usuario, resultado.getJustificacion());

        return solicitudFactory.toResponse(solicitud);
    }

    // ======================
    // RF-05 - Asignar responsable
    // ======================

    @Override
    @Transactional
    public SolicitudResponse asignarResponsable(Long id, AsignarResponsableRequest request, Long usuarioId) {
        SolicitudAcademica solicitud = buscarSolicitud(id);
        Usuario solicitanteAccion = buscarUsuario(usuarioId);
        Usuario responsable = buscarUsuario(request.getResponsableId());

        cadenaValidacionFactory.construirCadenaBasica().validar(solicitud, solicitanteAccion);

        if (!responsable.estaActivo()) {
            throw new IllegalStateException(
                    "El usuario con id " + request.getResponsableId() + " no está activo.");
        }

        if (!ROLES_RESPONSABLE_VALIDOS.contains(responsable.getRol())) {
            throw new IllegalStateException("El usuario con id " + request.getResponsableId()
                    + " tiene rol " + responsable.getRol().name()
                    + ". Solo un DOCENTE o ADMINISTRATIVO puede ser asignado como responsable.");
        }

        solicitud.asignarResponsable(responsable);
        solicitud = solicitudRepository.save(solicitud);

        String observacion = request.getObservacion() != null
                ? request.getObservacion()
                : "Responsable asignado: " + responsable.getNombre();

        notificarAsignacionResponsable(solicitud, responsable, solicitanteAccion, observacion);

        return solicitudFactory.toResponse(solicitud);
    }
}