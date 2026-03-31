package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.SolicitudRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.*;
import co.edu.uniquindio.gestion_solicitudes.service.SolicitudService;
import co.edu.uniquindio.gestion_solicitudes.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final JwtUtil jwtUtil;

    // POST /api/solicitudes
    @PostMapping
    public ResponseEntity<SolicitudResponse> registrar(
            @Valid @RequestBody SolicitudRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Extraer el userId desde el token JWT
        String token = authHeader.substring(7);
        Long solicitanteId = jwtUtil.extraerUserId(token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.registrar(request, solicitanteId));
    }

    // GET /api/solicitudes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener (@PathVariable Long id){
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    // GET /api/solicitudes?estado=&tipo=&prioridad=&page=0&size=20
    @GetMapping
    public  ResponseEntity<SolicitudPageResponse> consultar (
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) TipoSolicitud tipo,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false) Long solicitanteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ){
        return ResponseEntity.ok(solicitudService.consultar(estado, tipo, prioridad, responsableId, solicitanteId, PageRequest.of(page,size)));
    }

    // PUT /api/solicitudes/{id}/clasificar
    @PutMapping("/{id}/clasificar")
    public ResponseEntity<SolicitudResponse> clasificar(
            @PathVariable Long id,
            @Valid @RequestBody ClasificarRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Extraer el userId desde el token JWT
        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);

        return ResponseEntity.ok(solicitudService.clasificar(id, request, usuarioId));
    }
}
