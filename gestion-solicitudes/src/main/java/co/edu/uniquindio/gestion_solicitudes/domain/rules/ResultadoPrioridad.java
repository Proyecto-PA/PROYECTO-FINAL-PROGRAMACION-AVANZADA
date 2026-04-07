package co.edu.uniquindio.gestion_solicitudes.domain.rules;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ResultadoPrioridad {
    private final Prioridad prioridad;
    private final String justificacion;
}
