package co.edu.uniquindio.gestion_solicitudes.service.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.SolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.*;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.*;
import co.edu.uniquindio.gestion_solicitudes.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import co.edu.uniquindio.gestion_solicitudes.domain.validator.ValidadorTransicionEstado;
import co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialRepository historialRepository;

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
                .impactoAcademico(request.getImpactoAcademico())
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
    solicitud.cambiarEstado(EstadoSolicitud.CLASIFICADA);
    solicitud = solicitudRepository.save(solicitud);

    // Registrar en historial
    String observacion = request.getObservacion() != null
        ? request.getObservacion()
        : "Clasificada como " + request.getTipo().name();

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
}
