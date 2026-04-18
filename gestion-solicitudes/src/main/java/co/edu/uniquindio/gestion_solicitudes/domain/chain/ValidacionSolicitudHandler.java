package co.edu.uniquindio.gestion_solicitudes.domain.chain;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;

/**
 * Patrón Chain of Responsability: cada handler valida una regla de negocio.
 * Si la validación pasa, deleha al siguiente en la cadena.
 */
public abstract class ValidacionSolicitudHandler {

    protected  ValidacionSolicitudHandler siguiente;

    public ValidacionSolicitudHandler setSiguiente(ValidacionSolicitudHandler siguiente){
        this.siguiente = siguiente;
        return siguiente;
    }

    public abstract void validar(SolicitudAcademica solicitud, Usuario ejecutor);

    protected void continuarCadena(SolicitudAcademica solicitud, Usuario ejecutor){
        if (siguiente != null){
            siguiente.validar(solicitud, ejecutor);
        }
    }
}
