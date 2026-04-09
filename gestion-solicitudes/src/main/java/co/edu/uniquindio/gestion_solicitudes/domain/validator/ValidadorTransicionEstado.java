package co.edu.uniquindio.gestion_solicitudes.domain.validator;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ValidadorTransicionEstado {
   
    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> TRANSICIONES_VALIDAS =
        Map.of(
            EstadoSolicitud.REGISTRADA,  Set.of(EstadoSolicitud.CLASIFICADA, EstadoSolicitud.CANCELADA),
            EstadoSolicitud.CLASIFICADA, Set.of(EstadoSolicitud.EN_ATENCION),
            EstadoSolicitud.EN_ATENCION, Set.of(EstadoSolicitud.ATENDIDA),
            EstadoSolicitud.ATENDIDA,    Set.of(EstadoSolicitud.CERRADA)
        );

    public static boolean esValida(EstadoSolicitud estadoAnterior, EstadoSolicitud estadoNuevo) {
        Set<EstadoSolicitud> permitidos = TRANSICIONES_VALIDAS.getOrDefault(estadoAnterior, Set.of());
        return permitidos.contains(estadoNuevo);
    }

    public List<EstadoSolicitud> transicionesDesde (EstadoSolicitud estado){
        return List.copyOf(TRANSICIONES_VALIDAS.getOrDefault(estado, Set.of()));
    }

    public static void validarOLanzar(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (!esValida(actual, nuevo)){
            throw new IllegalStateException(
                    "No es posible pasar de " + actual + "  a " + nuevo + ". Transición no permitida"
            );
        }
    } 
}
