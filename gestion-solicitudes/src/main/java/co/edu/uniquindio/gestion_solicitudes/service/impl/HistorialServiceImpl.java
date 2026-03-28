package co.edu.uniquindio.gestion_solicitudes.service.impl;

import co.edu.uniquindio.gestion_solicitudes.dto.response.HistorialSolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.HistorialRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.SolicitudRepository;
import co.edu.uniquindio.gestion_solicitudes.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialServiceImpl implements HistorialService {

    private final HistorialRepository historialRepository;
    private final SolicitudRepository solicitudRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HistorialSolicitudResponse> consultarPorSolicitud(Long solicitudId){
        solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una solicitud con id " +  solicitudId));
        return historialRepository
                .findBySolicitudIdOrderByFechaAccionAsc(solicitudId)
                .stream()
                .map(h -> HistorialSolicitudResponse.builder()
                        .id(h.getId())
                        .fechaAccion(h.getFechaAccion())
                        .accionRealizada(h.getAccionRealizada())
                        .observaciones(h.getObservaciones())
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .usuarioResponsable(h.getUsuarioResponsable() != null ?
                                UsuarioResumenResponse.builder()
                                        .id(h.getUsuarioResponsable().getId())
                                        .nombre(h.getUsuarioResponsable().getNombre())
                                        .email(h.getUsuarioResponsable().getEmail())
                                        .rol(h.getUsuarioResponsable().getRol())
                                        .build() : null)
                        .build())
                .toList();
    }
}
