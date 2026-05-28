package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.dto.request.AsignarResponsableRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CambiarEstadoRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CerrarSolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.SolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudPageResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import org.springframework.data.domain.Pageable;

public interface SolicitudService {
    SolicitudResponse registrar(SolicitudRequest request, Long solicitanteId);
    SolicitudResponse obtenerPorId(Long id);
    SolicitudPageResponse consultar (EstadoSolicitud estado, TipoSolicitud tipo, Prioridad prioridad,
                                 Long responsableId, Long solicitanteId, Boolean sinResponsable,
                                 Long userId, String rol, Pageable pageable);
    SolicitudResponse clasificar(Long id, ClasificarRequest request, Long usuarioId);
    SolicitudResponse iniciarAtencion(Long id, CambiarEstadoRequest request, Long usuarioId);
    SolicitudResponse marcarAtendida(Long id, CambiarEstadoRequest request, Long usuarioId);
    SolicitudResponse cerrar(Long id, CerrarSolicitudRequest request, Long usuarioId);
    SolicitudResponse cancelar(Long id, CambiarEstadoRequest request, Long usuarioId);
    SolicitudResponse priorizar(Long id, Long usuarioId);
    SolicitudResponse asignarResponsable(Long id, AsignarResponsableRequest request, Long usuarioId);
}
