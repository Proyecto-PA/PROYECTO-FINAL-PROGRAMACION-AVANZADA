package co.edu.uniquindio.gestion_solicitudes.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SugerenciaIAResponse {
    private String tipoSugerido;
    private String prioridadSugerida;
    private String resumen;
    private Boolean confirmada;
    private LocalDateTime fechaSugerencia;
}
