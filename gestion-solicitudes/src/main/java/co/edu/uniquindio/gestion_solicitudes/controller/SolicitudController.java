package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.*;
import co.edu.uniquindio.gestion_solicitudes.dto.response.*;
import co.edu.uniquindio.gestion_solicitudes.service.IAService;
import co.edu.uniquindio.gestion_solicitudes.service.SolicitudService;
import co.edu.uniquindio.gestion_solicitudes.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final JwtUtil jwtUtil;
    private final IAService iaService;

    // POST /api/solicitudes
    @PostMapping
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudResponse> obtener (@PathVariable Long id){
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    // GET /api/solicitudes?estado=&tipo=&prioridad=&page=0&size=20
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public  ResponseEntity<SolicitudPageResponse> consultar (
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) TipoSolicitud tipo,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false) Long solicitanteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authHeader)
            {
                String token = authHeader.substring(7);
                String rol = jwtUtil.extraerRol(token);

                if("ESTUDIANTE".equals(rol)){
                    solicitanteId = jwtUtil.extraerUserId(token);
                }
        return ResponseEntity.ok(solicitudService.consultar(estado, tipo, prioridad, responsableId, solicitanteId, PageRequest.of(page,size)));
    }

    // PUT /api/solicitudes/{id}/clasificar
    @PutMapping("/{id}/clasificar")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRATIVO')")
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
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRATIVO')")
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
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRATIVO')")
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
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRATIVO')")
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudResponse> cancelar(
            @PathVariable Long id,
            @RequestBody(required = false) CambiarEstadoRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);
        return ResponseEntity.ok(solicitudService.cancelar(id, request, usuarioId));
    }

    // PUT /api/solicitudes/{id}/priorizar — Fase 6
    @PutMapping("/{id}/priorizar")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> priorizar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);
        return ResponseEntity.ok(solicitudService.priorizar(id, usuarioId));
    }

    // PUT /api/solicitudes/{id}/responsable — Fase 7
    @PutMapping("/{id}/responsable")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> asignarResponsable(
            @PathVariable Long id,
            @Valid @RequestBody AsignarResponsableRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);
        return ResponseEntity.ok(solicitudService.asignarResponsable(id, request, usuarioId));
    }

    /**
     * GET /api/solicitudes/{id}/sugerencia-ia
     * Consulta IA para obtener sugerencia de tipo, prioridad y resumen (RF-09, RF-10).
     */
    @GetMapping("/{id}/sugerencia-ia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SugerenciaIAResponse> obtenerSugerencia(@PathVariable Long id){
        return ResponseEntity.ok(iaService.generarSugerencia(id));
    }

    /**
     * PUT /api/solicitudes/{id}/sugerencia-ia/confirmar
     * El usuario decide si aplica o descarta la sugerencia IA (RF-10).
     */
    @PutMapping("/{id}/sugerencia-ia/confirmar")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> confirmarSugerencia(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarSugerenciaRequest request,
            @RequestHeader ("Authorization") String authHeader){
        String token = authHeader.substring(7);
        Long usuarioId = jwtUtil.extraerUserId(token);

        return ResponseEntity.ok(iaService.confirmarSugerencia(id, request, usuarioId));
    }

}
