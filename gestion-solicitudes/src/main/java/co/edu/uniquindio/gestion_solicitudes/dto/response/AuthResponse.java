package co.edu.uniquindio.gestion_solicitudes.dto.response;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AuthResponse {
    private String token;
    private Long usuarioId;
    private String nombre;
    private String email;
    private RolUsuario rol;
}
