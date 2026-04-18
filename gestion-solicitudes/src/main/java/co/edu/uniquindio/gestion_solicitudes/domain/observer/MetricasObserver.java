package co.edu.uniquindio.gestion_solicitudes.domain.observer;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Registra métricas y lanza alertas para solicitudes de prioridad CRITICA o ALTA.
 */
@Slf4j
@Component
public class MetricasObserver implements SolicitudObserver {

    @Override
    public void onCambioEstado(SolicitudAcademica solicitud, EstadoSolicitud estadoAnterior, EstadoSolicitud estadoNuevo,
                               Usuario responsable, String observacion){
        log.info("[METRICA] solicitud={} {} -> {} ejecutor = {}", solicitud.getId(), estadoAnterior, estadoNuevo, responsable != null ? responsable.getEmail() : "sistema");
        // Alerta si una solicitud CRITICA lleva mucho tiempo sin avanzar
        if(Prioridad.CRITICA.equals(solicitud.getPrioridad()) && estadoNuevo == EstadoSolicitud.CLASIFICADA) {
            log.warn("[ALERTA] Solicitud CRITICA {} sin responsable asignado aún.", solicitud.getId());
        }
    }

    @Override
    public void onAsignacionResponsable(SolicitudAcademica solicitud, Usuario responsableAsignado, Usuario ejecutor, String observacion){
        log.info("[METRICA] Responsable asignado: solicitud={} responsable ={}", solicitud.getId(), responsableAsignado.getEmail());
    }

    @Override
    public void onPrioridadCalculada(SolicitudAcademica solicitud, Usuario ejecutor, String justificacion){
        log.info("[METRICA] Prioridad calculada: solicitud={} prioridad={}", solicitud.getId(), solicitud.getPrioridad());

        if(Prioridad.CRITICA.equals(solicitud.getPrioridad())){
            log.warn("[ALERTA] Nueva solicitud CRITICA registrada: id={} descripcion='{}'", solicitud.getId()
            ,solicitud.getDescripcion().substring(0, Math.min(60, solicitud.getDescripcion().length())));
        }
    }
}
