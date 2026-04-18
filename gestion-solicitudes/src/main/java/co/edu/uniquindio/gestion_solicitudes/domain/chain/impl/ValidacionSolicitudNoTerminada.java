package co.edu.uniquindio.gestion_solicitudes.domain.chain.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.chain.ValidacionSolicitudHandler;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ValidacionSolicitudNoTerminada extends ValidacionSolicitudHandler {

    @Override
    public void validar(SolicitudAcademica solicitud, Usuario ejecutor){
        if (solicitud.estaCerrada()) {
            throw new IllegalStateException("La solicitud " + solicitud.getId() + " está en estado " +
                    solicitud.getEstado().name() + " y no puede modificarse.");
        }
        continuarCadena(solicitud, ejecutor);
    }
}
