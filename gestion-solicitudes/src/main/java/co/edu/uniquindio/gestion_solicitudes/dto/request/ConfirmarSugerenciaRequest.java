package co.edu.uniquindio.gestion_solicitudes.dto.request;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmarSugerenciaRequest {

    @NotNull(message = "El campo 'aplicar' es obligatorio")
    private Boolean aplicar;

    private TipoSolicitud tipoAjustado;
    private Prioridad prioridadAjustada;
}
