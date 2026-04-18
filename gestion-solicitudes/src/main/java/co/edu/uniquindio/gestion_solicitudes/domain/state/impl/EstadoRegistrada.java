package co.edu.uniquindio.gestion_solicitudes.domain.state.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.state.EstadoSolicitudState;
import co.edu.uniquindio.gestion_solicitudes.dto.request.ClasificarRequest;
import org.springframework.stereotype.Component;

@Component
public class EstadoRegistrada implements EstadoSolicitudState {

    @Override
    public void clasificar(SolicitudAcademica solicitud, Usuario ejecutor, Object payload){
        ClasificarRequest request = (ClasificarRequest) payload;
        solicitud.clasificarSolicitud(request.getTipo());
        solicitud.setImpactoAcademico(request.getImpactoAcademico());
        solicitud.cambiarEstado(EstadoSolicitud.CLASIFICADA);
    }

    @Override
    public void iniciarAtencion(SolicitudAcademica solicitud, Usuario ejecutor, String observacion){
        throw new IllegalStateException("No se puede inciiar atención desde REGISTRADA. Clasifique la solicitud primero.");
    }

    @Override
    public void marcarAtendida(SolicitudAcademica solicitud, Usuario ejecutor, String observacion){
        throw new IllegalStateException("Transición no permitida desde REGISTRADA.");
    }

    @Override
    public void cerrar(SolicitudAcademica solicitud, Usuario ejecutor, String observacion){
        throw new IllegalStateException("Transición no permitida desde REGISTRADA.");
    }

    @Override
    public void cancelar(SolicitudAcademica solicitud, Usuario ejecutor, String observacion){
        solicitud.cambiarEstado(EstadoSolicitud.CANCELADA);
    }

    @Override
    public String getNombre(){
        return "REGISTRADA";
    }
}
