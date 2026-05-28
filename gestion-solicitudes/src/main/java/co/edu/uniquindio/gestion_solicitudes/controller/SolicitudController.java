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

    // Helpers
    private Long extraerUserId(String authHeader){
        return  jwtUtil.extraerUserId(authHeader.substring(7));
    }

    private String extraerRol(String authHeader){
        return jwtUtil.extraerRol(authHeader.substring(7));
    }

    // POST /solicitudes
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudResponse> registrar(
            @Valid @RequestBody SolicitudRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.registrar(request, extraerUserId(authHeader)));
    }

    // GET /solicitudes/{id}
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudResponse> obtener (@PathVariable Long id){
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    // GET /solicitudes?estado=&tipo=&prioridad=&page=0&size=20
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
                Long userId = extraerUserId(authHeader);
                String rol = extraerRol(authHeader);

        return ResponseEntity.ok(solicitudService.consultar(estado, tipo, prioridad, responsableId, solicitanteId, userId, rol,PageRequest.of(page,size)));
    }

    // PUT /solicitudes/{id}/clasificar
    @PutMapping("/{id}/clasificar")
    @PreAuthorize("hasAnyRole( 'ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> clasificar(
            @PathVariable Long id,
            @Valid @RequestBody ClasificarRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(solicitudService.clasificar(id, request, extraerUserId(authHeader)));
    }

    // PUT /solicitudes/{id}/iniciar-atencion
    @PutMapping("/{id}/iniciar-atencion")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> iniciarAtencion(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CambiarEstadoRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(solicitudService.iniciarAtencion(id, request, extraerUserId(authHeader)));
    }

    // PUT /solicitudes/{id}/marcar-atendida
    @PutMapping("/{id}/marcar-atendida")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> marcarAtendida(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CambiarEstadoRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(solicitudService.marcarAtendida(id, request, extraerUserId(authHeader)));
    }

    // PUT /solicitudes/{id}/cerrar
    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> cerrar(
            @PathVariable Long id,
            @Valid @RequestBody CerrarSolicitudRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(solicitudService.cerrar(id, request, extraerUserId(authHeader)));
    }

    // PUT /solicitudes/{id}/cancelar
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudResponse> cancelar(
            @PathVariable Long id,
            @Valid@RequestBody(required = false) CambiarEstadoRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(solicitudService.cancelar(id, request, extraerUserId(authHeader)));
    }

    // PUT /solicitudes/{id}/priorizar
    @PutMapping("/{id}/priorizar")
    @PreAuthorize("hasAnyRole( 'ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> priorizar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(solicitudService.priorizar(id, extraerUserId(authHeader)));
    }

    // PUT /solicitudes/{id}/responsable
    @PutMapping("/{id}/responsable")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> asignarResponsable(
            @PathVariable Long id,
            @Valid @RequestBody AsignarResponsableRequest request,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(solicitudService.asignarResponsable(id, request, extraerUserId(authHeader)));
    }

    // GET /solicitudes/{id}/sugerencia-ia
    @GetMapping("/{id}/sugerencia-ia")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<SugerenciaIAResponse> obtenerSugerencia(@PathVariable Long id){
        return ResponseEntity.ok(iaService.generarSugerencia(id));
    }

    // PUT /solicitudes/{id}/sugerencia-ia/confirmar
    @PutMapping("/{id}/sugerencia-ia/confirmar")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO')")
    public ResponseEntity<SolicitudResponse> confirmarSugerencia(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarSugerenciaRequest request,
            @RequestHeader ("Authorization") String authHeader){

        return ResponseEntity.ok(iaService.confirmarSugerencia(id, request, extraerUserId(authHeader)));
    }

}
