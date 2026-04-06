package co.edu.uniquindio.gestion_solicitudes.dto.request;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClasificarRequest {

    @NotNull(message = "El tipo de solicitud es obligatorio")
    private TipoSolicitud tipo;

    @Min(value = 1, message = "El impacto académico mínimo es 1")
    @Max(value =5, message = "El impacto académico máximo es 5")
    @NotNull(message = "El impacto académico es obligatorio" )
    private Integer impactoAcademico;

    private String observacion;
}