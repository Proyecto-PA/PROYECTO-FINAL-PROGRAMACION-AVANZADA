package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.dto.response.HistorialSolicitudResponse;

import java.util.List;

public interface HistorialService {
    List<HistorialSolicitudResponse> consultarPorSolicitud(Long solicitufId);
}
