package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.dto.request.ConfirmarSugerenciaRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SugerenciaIAResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.service.IAService;
import co.edu.uniquindio.gestion_solicitudes.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class IAController {

    private final IAService iaService;
    private final JwtUtil jwtUtil;

    /**
     * GET /api/solicitudes/{id}/sugerencia-ia
     * Consulta IA para obtener sugerencia de tipo, prioridad y resumen (RF-09, RF-10).
     */
    @GetMapping("/{id}/sugerencia-ia")
    public ResponseEntity<SugerenciaIAResponse> obtenerSugerencia(@PathVariable Long id){
        return ResponseEntity.ok(iaService.generarSugerencia(id));
    }

    /**
     * PUT /api/solicitudes/{id}/sugerencia-ia/confirmar
     * El usuario decide si aplica o descarta la sugerencia IA (RF-10).
     */
    @PutMapping("/{id}/sugerencia-ia/confirmar")
    public ResponseEntity<SolicitudResponse> confirmarSugerencia(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarSugerenciaRequest request,
            @RequestHeader ("Authorization") String authHeader){
        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);

        return ResponseEntity.ok(iaService.confirmarSugerencia(id, request, usuarioId));
    }
}
