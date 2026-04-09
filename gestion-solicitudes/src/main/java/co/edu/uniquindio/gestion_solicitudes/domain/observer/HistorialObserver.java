package co.edu.uniquindio.gestion_solicitudes.domain.observer;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.HistorialSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.repository.HistorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Patrón Observer: Registra automáticamente en el historial
 * cada evento relevante del ciclo de vida de una solicitud.
 * Centraliza la lógica de auditoría que antes estaba duplicada
 * en cada método del servicio.
 */
@Component
@RequiredArgsConstructor
public class HistorialObserver implements SolicitudObserver {

    private final HistorialRepository historialRepository;

    @Override
    public void onCambioEstado(SolicitudAcademica solicitud,
                               EstadoSolicitud estadoAnterior,
                               EstadoSolicitud estadoNuevo,
                               Usuario responsable,
                               String observacion) {

        historialRepository.save(HistorialSolicitud.builder()
                .solicitud(solicitud)
                .fechaAccion(LocalDateTime.now())
                .accionRealizada("Cambio de estado: " + estadoAnterior + " → " + estadoNuevo)
                .usuarioResponsable(responsable)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .observaciones(observacion)
                .build());
    }

    @Override
    public void onAsignacionResponsable(SolicitudAcademica solicitud,
                                         Usuario responsableAsignado,
                                         Usuario ejecutor,
                                         String observacion) {

        historialRepository.save(HistorialSolicitud.builder()
                .solicitud(solicitud)
                .fechaAccion(LocalDateTime.now())
                .accionRealizada("RESPONSABLE_ASIGNADO: " + responsableAsignado.getNombre())
                .usuarioResponsable(ejecutor)
                .estadoAnterior(solicitud.getEstado())
                .estadoNuevo(solicitud.getEstado())
                .observaciones(observacion)
                .build());
    }

    @Override
    public void onPrioridadCalculada(SolicitudAcademica solicitud,
                                      Usuario ejecutor,
                                      String justificacion) {

        historialRepository.save(HistorialSolicitud.builder()
                .solicitud(solicitud)
                .fechaAccion(LocalDateTime.now())
                .accionRealizada("Prioridad asignada automáticamente: " + solicitud.getPrioridad().name())
                .usuarioResponsable(ejecutor)
                .estadoAnterior(solicitud.getEstado())
                .estadoNuevo(solicitud.getEstado())
                .observaciones(justificacion)
                .build());
    }
}
