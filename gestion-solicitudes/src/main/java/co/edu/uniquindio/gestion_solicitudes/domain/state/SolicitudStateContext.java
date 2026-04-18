package co.edu.uniquindio.gestion_solicitudes.domain.state;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.state.impl.EstadoAtendida;
import co.edu.uniquindio.gestion_solicitudes.domain.state.impl.EstadoClasificada;
import co.edu.uniquindio.gestion_solicitudes.domain.state.impl.EstadoEnAtencion;
import co.edu.uniquindio.gestion_solicitudes.domain.state.impl.EstadoRegistrada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Context del patrón State. Resuelve que objeto State corresponde al
 * EstadoSolicitud actual y delega la operación.
 */
@Component
@RequiredArgsConstructor
public class SolicitudStateContext {

    private final EstadoRegistrada estadoRegistrada;
    private final EstadoClasificada estadoClasificada;
    private final EstadoEnAtencion estadoEnAtencion;
    private final EstadoAtendida estadoAtendida;

    public EstadoSolicitudState resolverEstado(SolicitudAcademica solicitud){
        return switch (solicitud.getEstado()){
            case REGISTRADA -> estadoRegistrada;
            case CLASIFICADA -> estadoClasificada;
            case EN_ATENCION -> estadoEnAtencion;
            case ATENDIDA -> estadoAtendida;
            case CERRADA , CANCELADA -> throw new IllegalStateException(
                    "La solicitud está en estado terminal " + solicitud.getEstado().name() + " y no puede ser modificada."
            );
        };
    }
}
