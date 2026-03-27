package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.dto.request.SolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudPageResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import org.springframework.data.domain.Pageable;

public interface SolicitudService {
    SolicitudResponse registrar(SolicitudRequest request, Long solicitanteId);
    SolicitudResponse obtenerPorId(Long id);
    SolicitudPageResponse consultar (EstadoSolicitud estado, TipoSolicitud tipo, Prioridad prioridad,
                                     Long responsableId, Long solicitanteId, Pageable pageable);
}
