package co.edu.uniquindio.gestion_solicitudes.domain.validator;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import java.util.Map;
import java.util.Set;

public class ValidatorTransicionEstado {
   
    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> TRANSICIONES_VALIDAS =
        Map.of(
            EstadoSolicitud.REGISTRADA,  Set.of(EstadoSolicitud.CLASIFICADA, EstadoSolicitud.CANCELADA),
            EstadoSolicitud.CLASIFICADA, Set.of(EstadoSolicitud.EN_ATENCION),
            EstadoSolicitud.EN_ATENCION, Set.of(EstadoSolicitud.ATENDIDA),
            EstadoSolicitud.ATENDIDA,    Set.of(EstadoSolicitud.CERRADA)
        );

    public static void validarOLanzar(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        Set<EstadoSolicitud> permitidos = TRANSICIONES_VALIDAS.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new IllegalStateException(
                "No es posible pasar de " + actual + " a " + nuevo +
                ". Transición no permitida."
            );
        }
    } 
}
