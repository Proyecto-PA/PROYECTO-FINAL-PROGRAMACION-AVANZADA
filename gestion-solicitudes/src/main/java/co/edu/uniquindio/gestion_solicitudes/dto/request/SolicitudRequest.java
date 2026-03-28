package co.edu.uniquindio.gestion_solicitudes.dto.request;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.CanalOrigen;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolicitudRequest {

    @NotNull(message = "El tipo de solicitud es obligatorio")
    private TipoSolicitud tipo;

    @NotBlank(message = "La descripción es oblogatoria")
   @Size(min = 10, max = 1000, message = "La descripcion debe tener entre 10 y 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El canal de origen es obligatorio")
    private CanalOrigen canalOrigen;

    private LocalDateTime fechaLimite;

    @Min(value = 1, message = "El impacto académico mínimo es 1")
    @Max(value = 5, message = "El impacto académico máximo es 5")
    private Integer impactoAcademico;

}
