package co.edu.uniquindio.gestion_solicitudes.dto.request;
import java.io.Serial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CambiarEstadoRequest {

    @Size (max = 500 , message = "El motivo no puede tener más de 500 caracteres")
    private String observacion;
   
}
