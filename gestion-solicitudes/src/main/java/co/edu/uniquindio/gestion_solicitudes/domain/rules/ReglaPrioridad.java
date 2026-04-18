package co.edu.uniquindio.gestion_solicitudes.domain.rules;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import org.springframework.stereotype.Component;

@Component
public interface ReglaPrioridad {

    /**
     * Evalúa la solicitud y retorna la prioridad sugerida por esta regla.
     * Retorna null si la regla no aplica para la solicitud dada.
     */
    Prioridad evaluar(SolicitudAcademica solicitud);

    /**
     * Descripción de la regla para incluir en la justificación.
     */
    String getDescripcion(SolicitudAcademica solicitud);

    /**
     * Peso de la regla en el motor (mayor peso = mayor influencia).
     */
    int getPeso();
}
