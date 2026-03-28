package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.dto.response.HistorialSolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.service.HistorialService;
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

    private final HistorialService historialService;

    @GetMapping("/{solicitudId}")
    public ResponseEntity<List<HistorialSolicitudResponse>> consultar (
            @PathVariable Long solicitudId){
        return ResponseEntity.ok(historialService.consultarPorSolicitud(solicitudId));
    }
}
