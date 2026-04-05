package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.dto.request.ConfirmarSugerenciaRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SugerenciaIAResponse;

public interface IAService {
    SugerenciaIAResponse generarSugerencia (Long solicitudId);
    SolicitudResponse confirmarSugerencia (Long solicitudId, ConfirmarSugerenciaRequest request, Long usuarioId);
}
