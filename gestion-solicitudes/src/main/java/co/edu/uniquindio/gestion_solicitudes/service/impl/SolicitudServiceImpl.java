package co.edu.uniquindio.gestion_solicitudes.service.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.domain.factory.SolicitudFactory;
import co.edu.uniquindio.gestion_solicitudes.domain.observer.SolicitudObserver;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ResultadoPrioridad;
import co.edu.uniquindio.gestion_solicitudes.dto.request.AsignarResponsableRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.SolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.*;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.*;
import co.edu.uniquindio.gestion_solicitudes.service.MotorReglasPrioridad;
import co.edu.uniquindio.gestion_solicitudes.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import co.edu.uniquindio.gestion_solicitudes.domain.validator.ValidadorTransicionEstado;
import co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CambiarEstadoRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CerrarSolicitudRequest;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialRepository historialRepository;
    private final MotorReglasPrioridad motorReglasPrioridad;

    // Patrón Observer: lista de observadores inyectados por Spring
    private final List<SolicitudObserver> observadores;

    // Patrón Factory: centraliza creación y mapeo de objetos
    private final SolicitudFactory solicitudFactory;

    private static final Set<RolUsuario> ROLES_RESPONSABLE_VALIDOS =
            Set.of(RolUsuario.DOCENTE, RolUsuario.ADMINISTRATIVO);

    private void validarNoTerminada(SolicitudAcademica solicitudAcademica){
        if (solicitudAcademica.estaCerrada()){
            throw new IllegalStateException(
                    "La solicitud con id " + solicitudAcademica.getId() + " está en estado " + solicitudAcademica.getEstado().name() + " y no puede ser modificada."
            );
        }
    }

    /**
     * Notifica a todos los observadores registrados sobre un cambio de estado.
     * Patrón Observer: desacopla la lógica de auditoría del flujo principal.
     */
    private void notificarCambioEstado(SolicitudAcademica solicitud,
                                        EstadoSolicitud estadoAnterior,
                                        EstadoSolicitud estadoNuevo,
                                        Usuario responsable,
                                        String observacion) {
        for (SolicitudObserver obs : observadores) {
            obs.onCambioEstado(solicitud, estadoAnterior, estadoNuevo, responsable, observacion);
        }
    }

    private void notificarAsignacionResponsable(SolicitudAcademica solicitud,
                                                 Usuario responsableAsignado,
                                                 Usuario ejecutor,
                                                 String observacion) {
        for (SolicitudObserver obs : observadores) {
            obs.onAsignacionResponsable(solicitud, responsableAsignado, ejecutor, observacion);
        }
    }

    private void notificarPrioridadCalculada(SolicitudAcademica solicitud,
                                              Usuario ejecutor,
                                              String justificacion) {
        for (SolicitudObserver obs : observadores) {
            obs.onPrioridadCalculada(solicitud, ejecutor, justificacion);
        }
    }

    @Override
    @Transactional
    public SolicitudResponse registrar(SolicitudRequest request, Long solicitanteId){
        Usuario solicitante = usuarioRepository.findById(solicitanteId).orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un usuario con id  " + solicitanteId));

        // Patrón Factory: delega la creación de la entidad
        SolicitudAcademica solicitud = solicitudFactory.crearDesdeRequest(request, solicitante);
        solicitud = solicitudRepository.save(solicitud);

        // Entrada inicial en historial (caso especial: no es un cambio de estado)
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

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponse obtenerPorId(Long id){
        SolicitudAcademica solicitud = solicitudRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));
        return solicitudFactory.toResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudPageResponse consultar (EstadoSolicitud estado, TipoSolicitud tipo, Prioridad prioridad, Long responsableId, Long solicitanteId, Pageable pageable){
        Page<SolicitudAcademica> page = solicitudRepository.findWithFilters(estado, tipo, prioridad, responsableId, solicitanteId, pageable);

        return SolicitudPageResponse.builder()
                .contenido(page.getContent().stream().map(solicitudFactory::toResponse).toList())
                .paginaActual(page.getNumber())
                .totalPaginas(page.getTotalPages())
                .totalElementos(page.getTotalElements())
                .build();
    }

    //Método para clasificar la solicitud, que es el paso siguiente a registrarla solicitud
    @Override
    @Transactional
    public SolicitudResponse clasificar(Long id, ClasificarRequest request, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        validarNoTerminada(solicitud);

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        // Validar transición REGISTRADA → CLASIFICADA
        ValidadorTransicionEstado.validarOLanzar(solicitud.getEstado(), EstadoSolicitud.CLASIFICADA);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();

        // Actualizar tipo, impacto y estado
        solicitud.clasificarSolicitud(request.getTipo());
        solicitud.setImpactoAcademico(request.getImpactoAcademico());
        solicitud.cambiarEstado(EstadoSolicitud.CLASIFICADA);

        // Calcular prioridad automáticamente al clasificar (RF-03)
        ResultadoPrioridad resultadoPrioridad = motorReglasPrioridad.calcular(solicitud);
        solicitud.setPrioridad(resultadoPrioridad.getPrioridad());
        solicitud.setJustificacionPrioridad(resultadoPrioridad.getJustificacion());

        solicitud = solicitudRepository.save(solicitud);

        // Patrón Observer: notificar cambio de estado
        String observacion = request.getObservacion() != null
            ? request.getObservacion()
            : "Clasificada como " + request.getTipo().name() + " con impacto académico " + request.getImpactoAcademico();

        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.CLASIFICADA, usuario, observacion);

        // Patrón Observer: notificar prioridad calculada
        notificarPrioridadCalculada(solicitud, usuario, resultadoPrioridad.getJustificacion());

        return solicitudFactory.toResponse(solicitud);
    }

    // Fase 4: implementación de transiciones con el validador
    @Override
    @Transactional
    public SolicitudResponse iniciarAtencion(Long id, CambiarEstadoRequest request, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        validarNoTerminada(solicitud);

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        ValidadorTransicionEstado.validarOLanzar(solicitud.getEstado(), EstadoSolicitud.EN_ATENCION);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.cambiarEstado(EstadoSolicitud.EN_ATENCION);
        solicitud = solicitudRepository.save(solicitud);

        String observacion = request != null && request.getObservacion() != null
            ? request.getObservacion()
            : "Se inició la atención de la solicitud";

        // Patrón Observer: notificar cambio de estado
        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.EN_ATENCION, usuario, observacion);

        return solicitudFactory.toResponse(solicitud);
    }

    @Override
    @Transactional
    public SolicitudResponse marcarAtendida(Long id, CambiarEstadoRequest request, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        validarNoTerminada(solicitud);

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        ValidadorTransicionEstado.validarOLanzar(solicitud.getEstado(), EstadoSolicitud.ATENDIDA);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.cambiarEstado(EstadoSolicitud.ATENDIDA);
        solicitud = solicitudRepository.save(solicitud);

        String observacion = request != null && request.getObservacion() != null
            ? request.getObservacion()
            : "Solicitud marcada como atendida";

        // Patrón Observer: notificar cambio de estado
        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.ATENDIDA, usuario, observacion);

        return solicitudFactory.toResponse(solicitud);
    }

    @Override
    @Transactional
    public SolicitudResponse cerrar(Long id, CerrarSolicitudRequest request, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        validarNoTerminada(solicitud);

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        ValidadorTransicionEstado.validarOLanzar(solicitud.getEstado(), EstadoSolicitud.CERRADA);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.cambiarEstado(EstadoSolicitud.CERRADA);
        solicitud = solicitudRepository.save(solicitud);

        // Patrón Observer: notificar cambio de estado
        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.CERRADA, usuario, request.getObservacion());

        return solicitudFactory.toResponse(solicitud);
    }

    @Override
    @Transactional
    public SolicitudResponse cancelar(Long id, CambiarEstadoRequest request, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        validarNoTerminada(solicitud);

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        if(usuario.getRol() == RolUsuario.ESTUDIANTE && !solicitud.getSolicitante().getId().equals(usuarioId)){
            throw new IllegalStateException("Un estudiante solo puede cancelar sus propias solicitudes");
        }

        ValidadorTransicionEstado.validarOLanzar(solicitud.getEstado(), EstadoSolicitud.CANCELADA);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.cambiarEstado(EstadoSolicitud.CANCELADA);
        solicitud = solicitudRepository.save(solicitud);

        String observacion = request != null && request.getObservacion() != null
            ? request.getObservacion()
            : "Solicitud cancelada";

        // Patrón Observer: notificar cambio de estado
        notificarCambioEstado(solicitud, estadoAnterior, EstadoSolicitud.CANCELADA, usuario, observacion);

        return solicitudFactory.toResponse(solicitud);
    }

    // Fase 6: Priorización manual por motor de reglas
    @Override
    @Transactional
    public SolicitudResponse priorizar(Long id, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        validarNoTerminada(solicitud);

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        if(solicitud.getEstado() != EstadoSolicitud.CLASIFICADA){
            throw new IllegalStateException("Solo se puede priorizar una solicitud en estado CLASIFICADA. " + "Estado actual: " + solicitud.getEstado());
        }

        ResultadoPrioridad resultado = motorReglasPrioridad.calcular(solicitud);
        solicitud.setPrioridad(resultado.getPrioridad());
        solicitud.setJustificacionPrioridad(resultado.getJustificacion());
        solicitud = solicitudRepository.save(solicitud);

        // Patrón Observer: notificar prioridad calculada
        notificarPrioridadCalculada(solicitud, usuario, resultado.getJustificacion());

        return solicitudFactory.toResponse(solicitud);
    }

    // Fase 7: Asignación de responsable
    @Override
    @Transactional
    public SolicitudResponse asignarResponsable(Long id, AsignarResponsableRequest request, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        validarNoTerminada(solicitud);

        Usuario solicitanteAccion = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        Usuario responsable = usuarioRepository.findById(request.getResponsableId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + request.getResponsableId()));

        if (!responsable.estaActivo()) {
            throw new IllegalStateException(
                "El usuario con id " + request.getResponsableId() + " no está activo");
        }

        if(!ROLES_RESPONSABLE_VALIDOS.contains(responsable.getRol())){
            throw new IllegalStateException("El usuario con id " + request.getResponsableId() + " tiene rol "
            + responsable.getRol().name() + " Solo un DOCENTE o ADMINISTRATIVO puede ser asignado como responsable.");
        }

        solicitud.asignarResponsable(responsable);
        solicitud = solicitudRepository.save(solicitud);

        String observacion = request.getObservacion() != null
            ? request.getObservacion()
            : "Responsable asignado: " + responsable.getNombre();

        // Patrón Observer: notificar asignación de responsable
        notificarAsignacionResponsable(solicitud, responsable, solicitanteAccion, observacion);

        return solicitudFactory.toResponse(solicitud);
    }

}
