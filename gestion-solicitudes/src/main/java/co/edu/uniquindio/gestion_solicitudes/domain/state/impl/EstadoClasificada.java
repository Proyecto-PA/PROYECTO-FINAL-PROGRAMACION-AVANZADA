package co.edu.uniquindio.gestion_solicitudes.domain.state.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.state.EstadoSolicitudState;
import org.springframework.stereotype.Component;

@Component
public class EstadoClasificada implements EstadoSolicitudState {

    @Override
    public void clasificar(SolicitudAcademica solicitud, Usuario ejecutor, Object payload){
        throw new IllegalStateException("La solicitud ya está clasificada.");
    }

    @Override
    public void iniciarAtencion(SolicitudAcademica solicitud, Usuario ejecutor, String observacion){
        solicitud.cambiarEstado(EstadoSolicitud.EN_ATENCION);
    }

    @Override
    public void marcarAtendida(SolicitudAcademica solicitud, Usuario ejecutor, String observacion){
        throw new IllegalStateException("Transicion no permitida desde CLASIFICADA.");
    }

    @Override
    public void cerrar(SolicitudAcademica solicitud, Usuario ejecutor, String observacion){
        throw new IllegalStateException("Transición no permitida desde CLASIFICADA.");
    }

    @Override
    public void cancelar (SolicitudAcademica solicitud, Usuario ejecutor, String observacion){
        throw new IllegalStateException("Solo se puede cancelar desde REGISTRADA.");
    }

    @Override
    public String getNombre(){
        return "CLASIFICADA";
    }

}
