package co.edu.uniquindio.gestion_solicitudes.domain.rules;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorCanalOrigen;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorFechaLimite;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorImpactoAcademico;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorTipoSolicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Patrón Strategy - Motor de Reglas de Prioridad
 */
@Component
@RequiredArgsConstructor
public class MotorReglasPrioridad {

    private  final List<ReglaPrioridad> reglas;

    // Puntaje numérico por nivel de prioridad
    private static final Map<Prioridad, Integer> PUNTAJE = Map.of(
            Prioridad.CRITICA, 4,
            Prioridad.ALTA, 3,
            Prioridad.MEDIA, 2,
            Prioridad.BAJA, 1);

    public ResultadoPrioridad calcular(SolicitudAcademica solicitud) {
        List<ReglaPrioridad> reglasOrdenadas = reglas.stream()
                .sorted(Comparator.comparingInt(ReglaPrioridad::getPeso).reversed()
                ).toList();

        int puntajeTotal = 0;
        int pesoTotal = 0;
        StringBuilder justificacion = new StringBuilder();

        for (ReglaPrioridad regla : reglasOrdenadas) {
            Prioridad resultado = regla.evaluar(solicitud);
            if (resultado != null) {
                int contribucion = PUNTAJE.get(resultado) * regla.getPeso();
                puntajeTotal += contribucion;
                pesoTotal += regla.getPeso();
                justificacion.append("- ").append(regla.getDescripcion(solicitud))
                        .append(" -> ").append(resultado.name()).append(". ");
            }
        }

        Prioridad prioridadFinal = pesoTotal == 0
                ? Prioridad.MEDIA
                : resolverPorPuntaje((double) puntajeTotal / pesoTotal);

        justificacion.append("Prioridad final calculada: ").append(prioridadFinal.name()).append(".");
        return new ResultadoPrioridad(prioridadFinal, justificacion.toString().trim());
    }

    private Prioridad resolverPorPuntaje(double promedio) {
        if (promedio >= 3.5)
            return Prioridad.CRITICA;
        if (promedio >= 2.5)
            return Prioridad.ALTA;
        if (promedio >= 1.5)
            return Prioridad.MEDIA;
        return Prioridad.BAJA;
    }
}
