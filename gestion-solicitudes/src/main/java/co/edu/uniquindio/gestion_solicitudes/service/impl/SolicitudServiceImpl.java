package co.edu.uniquindio.gestion_solicitudes.service.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
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

    @Override
    @Transactional
    public SolicitudResponse registrar(SolicitudRequest request, Long solicitanteId){
        Usuario solicitante = usuarioRepository.findById(solicitanteId).orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un usuario con id  " + solicitanteId));

        SolicitudAcademica solicitud = SolicitudAcademica.builder()
                .tipo(request.getTipo())
                .descripcion(request.getDescripcion())
                .canalOrigen(request.getCanalOrigen())
                .fechaRegistro(LocalDateTime.now())
                .fechaLimite(request.getFechaLimite())
                .estado(EstadoSolicitud.REGISTRADA)
                .solicitante(solicitante)
                .build();

        solicitud = solicitudRepository.save(solicitud);

        // Entrada inicial en historial
        HistorialSolicitud historial = HistorialSolicitud.builder()
                .solicitud(solicitud)
                .fechaAccion(LocalDateTime.now())
                .accionRealizada("Solicitud registrada en el sistema")
                .usuarioResponsable(solicitante)
                .estadoAnterior(null)
                .estadoNuevo(EstadoSolicitud.REGISTRADA)
                .build();

        historialRepository.save(historial);
        return toResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponse obtenerPorId(Long id){
        SolicitudAcademica solicitud = solicitudRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));
        return toResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudPageResponse consultar (EstadoSolicitud estado, TipoSolicitud tipo, Prioridad prioridad, Long responsableId, Long solicitanteId, Pageable pageable){
        Page<SolicitudAcademica> page = solicitudRepository.findWithFilters(estado, tipo, prioridad, responsableId, solicitanteId, pageable);

        return SolicitudPageResponse.builder()
                .contenido(page.getContent().stream().map(this::toResponse).toList())
                .paginaActual(page.getNumber())
                .totalPaginas(page.getTotalPages())
                .totalElementos(page.getTotalElements())
                .build();
    }

    // Mapper
    private SolicitudResponse toResponse (SolicitudAcademica solicitud){
        return SolicitudResponse.builder()
                .id(solicitud.getId())
                .tipo(solicitud.getTipo())
                .descripcion(solicitud.getDescripcion())
                .canalOrigen(solicitud.getCanalOrigen())
                .fechaRegistro(solicitud.getFechaRegistro())
                .fechaLimite(solicitud.getFechaLimite())
                .estado(solicitud.getEstado())
                .prioridad(solicitud.getPrioridad())
                .justificacionPrioridad(solicitud.getJustificacionPrioridad())
                .impactoAcademico(solicitud.getImpactoAcademico())
                .solicitante(toUsuarioResumen(solicitud.getSolicitante()))
                .responsable(solicitud.getResponsable() != null ? toUsuarioResumen(solicitud.getResponsable()) : null)
                .build();
    }

    private UsuarioResumenResponse toUsuarioResumen(Usuario usuario){
        return UsuarioResumenResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }

    //Método para clasificar la solicitud, que es el paso siguiente a registrarla solicitud
    @Override
    @Transactional
    public SolicitudResponse clasificar(Long id, ClasificarRequest request, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        // Validar transición REGISTRADA → CLASIFICADA
        ValidadorTransicionEstado.validarOLanzar(solicitud.getEstado(), EstadoSolicitud.CLASIFICADA);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();

        // Actualizar tipo y estado
        solicitud.clasificarSolicitud(request.getTipo());
        solicitud.setImpactoAcademico(request.getImpactoAcademico());
        solicitud.cambiarEstado(EstadoSolicitud.CLASIFICADA);
        solicitud = solicitudRepository.save(solicitud);

        // Registrar en historial
        String observacion = request.getObservacion() != null
            ? request.getObservacion()
            : "Clasificada como " + request.getTipo().name() + " con impacto académico " + request.getImpactoAcademico();

        HistorialSolicitud historial = HistorialSolicitud.builder()
            .solicitud(solicitud)
            .fechaAccion(LocalDateTime.now())
            .accionRealizada("Cambio de estado: " + estadoAnterior + " → " + EstadoSolicitud.CLASIFICADA)
            .usuarioResponsable(usuario)
            .estadoAnterior(estadoAnterior)
            .estadoNuevo(EstadoSolicitud.CLASIFICADA)
            .observaciones(observacion)
            .build();

        historialRepository.save(historial);

        return toResponse(solicitud);
    }

    // Fase 4: implementación de transiciones con el validador
    @Override
    @Transactional
    public SolicitudResponse iniciarAtencion(Long id, CambiarEstadoRequest request, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        // Validar transición REGISTRADA → EN_ATENCION
        ValidadorTransicionEstado.validarOLanzar(solicitud.getEstado(), EstadoSolicitud.EN_ATENCION);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.cambiarEstado(EstadoSolicitud.EN_ATENCION);
        solicitud = solicitudRepository.save(solicitud);

        String observacion = request != null && request.getObservacion() != null
            ? request.getObservacion()
            : "Se inició la atención de la solicitud";

    historialRepository.save(HistorialSolicitud.builder()
        .solicitud(solicitud)
        .fechaAccion(LocalDateTime.now())
        .accionRealizada("Cambio de estado: " + estadoAnterior + " → " + EstadoSolicitud.EN_ATENCION)
        .usuarioResponsable(usuario)
        .estadoAnterior(estadoAnterior)
        .estadoNuevo(EstadoSolicitud.EN_ATENCION)
        .observaciones(observacion)
        .build());

    return toResponse(solicitud);
}

@Override
@Transactional
public SolicitudResponse marcarAtendida(Long id, CambiarEstadoRequest request, Long usuarioId) {

    SolicitudAcademica solicitud = solicitudRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe una solicitud con id " + id));

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

    historialRepository.save(HistorialSolicitud.builder()
        .solicitud(solicitud)
        .fechaAccion(LocalDateTime.now())
        .accionRealizada("Cambio de estado: " + estadoAnterior + " → " + EstadoSolicitud.ATENDIDA)
        .usuarioResponsable(usuario)
        .estadoAnterior(estadoAnterior)
        .estadoNuevo(EstadoSolicitud.ATENDIDA)
        .observaciones(observacion)
        .build());

    return toResponse(solicitud);
}

@Override
@Transactional
public SolicitudResponse cerrar(Long id, CerrarSolicitudRequest request, Long usuarioId) {

    SolicitudAcademica solicitud = solicitudRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe una solicitud con id " + id));

    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe un usuario con id " + usuarioId));

    ValidadorTransicionEstado.validarOLanzar(solicitud.getEstado(), EstadoSolicitud.CERRADA);

    EstadoSolicitud estadoAnterior = solicitud.getEstado();
    solicitud.cambiarEstado(EstadoSolicitud.CERRADA);
    solicitud = solicitudRepository.save(solicitud);

    historialRepository.save(HistorialSolicitud.builder()
        .solicitud(solicitud)
        .fechaAccion(LocalDateTime.now())
        .accionRealizada("Cambio de estado: " + estadoAnterior + " → " + EstadoSolicitud.CERRADA)
        .usuarioResponsable(usuario)
        .estadoAnterior(estadoAnterior)
        .estadoNuevo(EstadoSolicitud.CERRADA)
        .observaciones(request.getObservacion())
        .build());

    return toResponse(solicitud);
}

@Override
@Transactional
public SolicitudResponse cancelar(Long id, CambiarEstadoRequest request, Long usuarioId) {

    SolicitudAcademica solicitud = solicitudRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe una solicitud con id " + id));

    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No existe un usuario con id " + usuarioId));

    ValidadorTransicionEstado.validarOLanzar(solicitud.getEstado(), EstadoSolicitud.CANCELADA);

    EstadoSolicitud estadoAnterior = solicitud.getEstado();
    solicitud.cambiarEstado(EstadoSolicitud.CANCELADA);
    solicitud = solicitudRepository.save(solicitud);

    String observacion = request != null && request.getObservacion() != null
        ? request.getObservacion()
        : "Solicitud cancelada";

    historialRepository.save(HistorialSolicitud.builder()
        .solicitud(solicitud)
        .fechaAccion(LocalDateTime.now())
        .accionRealizada("Cambio de estado: " + estadoAnterior + " → " + EstadoSolicitud.CANCELADA)
        .usuarioResponsable(usuario)
        .estadoAnterior(estadoAnterior)
        .estadoNuevo(EstadoSolicitud.CANCELADA)
        .observaciones(observacion)
        .build());

    return toResponse(solicitud);
}

    // Fase 6: Priorización automática por motor de reglas
    @Override
    @Transactional
    public SolicitudResponse priorizar(Long id, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

        if(solicitud.getEstado() != EstadoSolicitud.CLASIFICADA){
            throw new IllegalStateException("Solo se puede priorizar una solicitud en estado CLASIFICADA. " + "Estado actual: " + solicitud.getEstado());
        }
        MotorReglasPrioridad.ResultadoPrioridad resultado = motorReglasPrioridad.calcular(solicitud);

        solicitud.setPrioridad(resultado.prioridad());
        solicitud.setJustificacionPrioridad(resultado.justificacion());
        solicitud = solicitudRepository.save(solicitud);

        historialRepository.save(HistorialSolicitud.builder()
            .solicitud(solicitud)
            .fechaAccion(LocalDateTime.now())
            .accionRealizada("Prioridad asignada automáticamente: " + resultado.prioridad().name())
            .usuarioResponsable(usuario)
            .estadoAnterior(solicitud.getEstado())
            .estadoNuevo(solicitud.getEstado())
            .observaciones(resultado.justificacion())
            .build());

        return toResponse(solicitud);
    }

    // Fase 7: Asignación de responsable
    @Override
    @Transactional
    public SolicitudResponse asignarResponsable(Long id, AsignarResponsableRequest request, Long usuarioId) {

        SolicitudAcademica solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe una solicitud con id " + id));

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

        solicitud.asignarResponsable(responsable);
        solicitud = solicitudRepository.save(solicitud);

        String observacion = request.getObservacion() != null
            ? request.getObservacion()
            : "Responsable asignado: " + responsable.getNombre();

        historialRepository.save(HistorialSolicitud.builder()
            .solicitud(solicitud)
            .fechaAccion(LocalDateTime.now())
            .accionRealizada("RESPONSABLE_ASIGNADO: " + responsable.getNombre())
            .usuarioResponsable(solicitanteAccion)
            .estadoAnterior(solicitud.getEstado())
            .estadoNuevo(solicitud.getEstado())
            .observaciones(observacion)
            .build());

        return toResponse(solicitud);
    }

}
