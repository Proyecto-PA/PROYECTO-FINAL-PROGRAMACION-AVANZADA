package co.edu.uniquindio.gestion_solicitudes.domain.chain.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.chain.ValidacionSolicitudHandler;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Valida que al pasar a EN_ATENCION la solicitud tenga responsable asignado.
 */
@Component
@Order(3)
public class ValidacionSolicitudTieneResponsable extends ValidacionSolicitudHandler {

    @Override
    public void validar(SolicitudAcademica solicitud, Usuario ejecutor){
        if (solicitud.getEstado() == EstadoSolicitud.CLASIFICADA && solicitud.getResponsable() == null){
            throw new IllegalStateException("La solicitud debe tener un responsable asignado antes de iniciar atención.");
        }
        continuarCadena(solicitud, ejecutor);
    }
}
