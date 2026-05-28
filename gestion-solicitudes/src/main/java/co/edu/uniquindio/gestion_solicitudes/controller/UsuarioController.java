package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;
import co.edu.uniquindio.gestion_solicitudes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/docentes")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ResponseEntity<List<UsuarioResumenResponse>> listarDocentes() {
        return ResponseEntity.ok(
            usuarioRepository.findByRol(RolUsuario.DOCENTE)
                .stream()
                .map(u -> UsuarioResumenResponse.builder()
                    .id(u.getId())
                    .nombre(u.getNombre())
                    .email(u.getEmail())
                    .rol(u.getRol())
                    .build())
                .toList()
        );
    }
}
