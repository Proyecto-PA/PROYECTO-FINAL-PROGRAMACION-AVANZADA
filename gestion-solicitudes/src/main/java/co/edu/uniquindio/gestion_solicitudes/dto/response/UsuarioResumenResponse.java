package co.edu.uniquindio.gestion_solicitudes.dto.response;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class UsuarioResumenResponse {
    private Long id;
    private String nombre;
    private String email;
    private RolUsuario rol;
}
