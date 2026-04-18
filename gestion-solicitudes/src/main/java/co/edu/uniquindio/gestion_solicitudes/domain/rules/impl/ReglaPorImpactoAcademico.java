package co.edu.uniquindio.gestion_solicitudes.domain.rules.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ReglaPrioridad;
import org.springframework.stereotype.Component;

@Component
public class ReglaPorImpactoAcademico implements ReglaPrioridad {

    @Override
    public Prioridad evaluar(SolicitudAcademica solicitud) {
        Integer impacto = solicitud.getImpactoAcademico();
        if (impacto == null) return null;

        return switch (impacto) {
            case 5 -> Prioridad.CRITICA;
            case 4 -> Prioridad.ALTA;
            case 3 -> Prioridad.MEDIA;
            default -> Prioridad.BAJA;
        };
    }

    @Override
    public String getDescripcion(SolicitudAcademica solicitud) {
        Integer impacto = solicitud.getImpactoAcademico();
        if (impacto == null) return "Impacto académico no especificado.";
        return "Impacto académico nivel " + impacto + "/5.";
    }

    @Override
    public int getPeso() {
        return 3;
    }
}
