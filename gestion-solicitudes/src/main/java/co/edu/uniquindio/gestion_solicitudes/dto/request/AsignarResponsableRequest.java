package co.edu.uniquindio.gestion_solicitudes.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarResponsableRequest {

    @NotNull(message = "El ID del responsable es obligatorio")
    private Long responsableId;

    private String observacion;
}
