package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.dto.response.HistorialSolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.HistorialRepository;
import co.edu.uniquindio.gestion_solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/historial")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialRepository historialRepository;
    private final SolicitudRepository solicitudRepository;

    @GetMapping("/{solicitudId}")
    public ResponseEntity<List<HistorialSolicitudResponse>> consultar(
            @PathVariable Long solicitudId) {

        // Verificar que la solicitud existe
        solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una solicitud con id " + solicitudId));

        List<HistorialSolicitudResponse> historial = historialRepository
                .findBySolicitudIdOrderByFechaAccionAsc(solicitudId)
                .stream()
                .map(h -> HistorialSolicitudResponse.builder()
                        .id(h.getId())
                        .fechaAccion(h.getFechaAccion())
                        .accionRealizada(h.getAccionRealizada())
                        .usuarioResponsable(h.getUsuarioResponsable() != null ?
                                UsuarioResumenResponse.builder()
                                        .id(h.getUsuarioResponsable().getId())
                                        .nombre(h.getUsuarioResponsable().getNombre())
                                        .email(h.getUsuarioResponsable().getEmail())
                                        .rol(h.getUsuarioResponsable().getRol())
                                        .build() : null)
                        .observaciones(h.getObservaciones())
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .build())
                .toList();

        return ResponseEntity.ok(historial);
    }
}
