package co.edu.uniquindio.gestion_solicitudes.dto.request;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClasificarRequest {

    @NotNull(message = "El tipo de solicitud es obligatorio")
    private TipoSolicitud tipo;

    private String observacion;
}