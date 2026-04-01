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
import co.edu.uniquindio.gestion_solicitudes.dto.request.CambiarEstadoRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.request.CerrarSolicitudRequest;

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

    // PUT /api/solicitudes/{id}/iniciar-atencion
    @PutMapping("/{id}/iniciar-atencion")
    public ResponseEntity<SolicitudResponse> iniciarAtencion(
            @PathVariable Long id,
            @RequestBody(required = false) CambiarEstadoRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);
        return ResponseEntity.ok(solicitudService.iniciarAtencion(id, request, usuarioId));
    }

    // PUT /api/solicitudes/{id}/marcar-atendida
    @PutMapping("/{id}/marcar-atendida")
    public ResponseEntity<SolicitudResponse> marcarAtendida(
            @PathVariable Long id,
            @RequestBody(required = false) CambiarEstadoRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);
        return ResponseEntity.ok(solicitudService.marcarAtendida(id, request, usuarioId));
    }

    // PUT /api/solicitudes/{id}/cerrar
    @PutMapping("/{id}/cerrar")
    public ResponseEntity<SolicitudResponse> cerrar(
            @PathVariable Long id,
            @Valid @RequestBody CerrarSolicitudRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);
        return ResponseEntity.ok(solicitudService.cerrar(id, request, usuarioId));
    }

    // PUT /api/solicitudes/{id}/cancelar
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<SolicitudResponse> cancelar(
            @PathVariable Long id,
            @RequestBody(required = false) CambiarEstadoRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);
        return ResponseEntity.ok(solicitudService.cancelar(id, request, usuarioId));
    }

}
