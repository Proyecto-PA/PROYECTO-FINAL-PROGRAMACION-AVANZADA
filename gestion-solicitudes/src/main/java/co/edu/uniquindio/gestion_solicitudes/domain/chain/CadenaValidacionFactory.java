package co.edu.uniquindio.gestion_solicitudes.domain.chain;

import co.edu.uniquindio.gestion_solicitudes.domain.chain.impl.ValidacionEjecutorActivo;
import co.edu.uniquindio.gestion_solicitudes.domain.chain.impl.ValidacionSolicitudNoTerminada;
import co.edu.uniquindio.gestion_solicitudes.domain.chain.impl.ValidacionSolicitudTieneResponsable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Ensambla la cadena de validaciones en el orden correcto.
 */
@Component
@RequiredArgsConstructor
public class CadenaValidacionFactory {

    private final ValidacionSolicitudNoTerminada noTerminada;
    private final ValidacionEjecutorActivo ejecutorActivo;
    private final ValidacionSolicitudTieneResponsable tieneResponsable;

    public ValidacionSolicitudHandler construirCadenaBasica(){
        noTerminada.setSiguiente(ejecutorActivo);
        return  noTerminada;
    }

    public ValidacionSolicitudHandler construirCadenaIniciarAtencion(){
        noTerminada.setSiguiente(ejecutorActivo).setSiguiente(tieneResponsable);
        return  noTerminada;
    }
}
