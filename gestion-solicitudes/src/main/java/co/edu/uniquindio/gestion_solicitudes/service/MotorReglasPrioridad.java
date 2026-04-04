package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ReglaPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorCanalOrigen;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorFechaLimite;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorImpactoAcademico;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorTipoSolicitud;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MotorReglasPrioridad {

    private static final List<ReglaPrioridad> REGLAS = List.of(
            new ReglaPorFechaLimite(),
            new ReglaPorImpactoAcademico(),
            new ReglaPorTipoSolicitud(),
            new ReglaPorCanalOrigen()
    );

    // Puntaje numérico por nivel de prioridad
    private static final Map<Prioridad, Integer> PUNTAJE = Map.of(
            Prioridad.CRITICA, 4,
            Prioridad.ALTA,    3,
            Prioridad.MEDIA,   2,
            Prioridad.BAJA,    1
    );

    public record ResultadoPrioridad(Prioridad prioridad, String justificacion) {}

    public ResultadoPrioridad calcular(SolicitudAcademica solicitud) {
        int puntajeTotal = 0;
        int pesoTotal    = 0;
        StringBuilder justificacion = new StringBuilder();

        for (ReglaPrioridad regla : REGLAS) {
            Prioridad resultado = regla.evaluar(solicitud);
            if (resultado != null) {
                int contribucion = PUNTAJE.get(resultado) * regla.getPeso();
                puntajeTotal += contribucion;
                pesoTotal    += regla.getPeso();
                justificacion.append("- ").append(regla.getDescripcion(solicitud))
                             .append(" → ").append(resultado.name()).append(". ");
            }
        }

        Prioridad prioridadFinal = pesoTotal == 0
                ? Prioridad.MEDIA
                : resolverPorPuntaje((double) puntajeTotal / pesoTotal);

        justificacion.append("Prioridad final calculada: ").append(prioridadFinal.name()).append(".");
        return new ResultadoPrioridad(prioridadFinal, justificacion.toString().trim());
    }

    private Prioridad resolverPorPuntaje(double promedio) {
        if (promedio >= 3.5) return Prioridad.CRITICA;
        if (promedio >= 2.5) return Prioridad.ALTA;
        if (promedio >= 1.5) return Prioridad.MEDIA;
        return Prioridad.BAJA;
    }
}
