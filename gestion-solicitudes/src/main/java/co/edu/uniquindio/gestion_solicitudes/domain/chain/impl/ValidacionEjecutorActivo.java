package co.edu.uniquindio.gestion_solicitudes.domain.chain.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.chain.ValidacionSolicitudHandler;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ValidacionEjecutorActivo extends ValidacionSolicitudHandler {

    @Override
    public void validar (SolicitudAcademica solicitud, Usuario ejecutor){
        if (!ejecutor.estaActivo()) {
            throw new IllegalStateException(
                    "El usuario " + ejecutor.getEmail() + " no está activo."
            );
        }
        continuarCadena(solicitud, ejecutor);
    }
}
