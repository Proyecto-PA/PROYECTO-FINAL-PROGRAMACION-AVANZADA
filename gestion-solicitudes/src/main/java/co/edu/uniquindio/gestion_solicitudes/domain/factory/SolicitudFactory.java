package co.edu.uniquindio.gestion_solicitudes.domain.factory;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.dto.request.SolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Patrón Factory: Centraliza la creación de entidades SolicitudAcademica
 * y el mapeo entre entidades y DTOs de respuesta.
 * Separa la responsabilidad de construcción de objetos del servicio.
 */
@Component
public class SolicitudFactory {

    /**
     * Crea una nueva entidad SolicitudAcademica a partir del request y el solicitante.
     */
    public SolicitudAcademica crearDesdeRequest(SolicitudRequest request, Usuario solicitante) {
        return SolicitudAcademica.builder()
                .tipo(request.getTipo())
                .descripcion(request.getDescripcion())
                .canalOrigen(request.getCanalOrigen())
                .fechaRegistro(LocalDateTime.now())
                .fechaLimite(request.getFechaLimite())
                .estado(EstadoSolicitud.REGISTRADA)
                .solicitante(solicitante)
                .build();
    }

    /**
     * Convierte una entidad SolicitudAcademica a su DTO de respuesta.
     */
    public SolicitudResponse toResponse(SolicitudAcademica solicitud) {
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
                .responsable(solicitud.getResponsable() != null
                        ? toUsuarioResumen(solicitud.getResponsable()) : null)
                .build();
    }

    /**
     * Convierte una entidad Usuario a un resumen para respuestas.
     */
    public UsuarioResumenResponse toUsuarioResumen(Usuario usuario) {
        return UsuarioResumenResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }
}
