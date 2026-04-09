package co.edu.uniquindio.gestion_solicitudes.domain.observer;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;

/**
 * Patrón Observer: Define el contrato para observadores que reaccionan
 * ante eventos del ciclo de vida de una solicitud académica.
 */
public interface SolicitudObserver {

    /**
     * Se invoca cuando una solicitud cambia de estado.
     */
    void onCambioEstado(SolicitudAcademica solicitud,
                        EstadoSolicitud estadoAnterior,
                        EstadoSolicitud estadoNuevo,
                        Usuario responsable,
                        String observacion);

    /**
     * Se invoca cuando se asigna un responsable a una solicitud.
     */
    void onAsignacionResponsable(SolicitudAcademica solicitud,
                                  Usuario responsableAsignado,
                                  Usuario ejecutor,
                                  String observacion);

    /**
     * Se invoca cuando se calcula o recalcula la prioridad de una solicitud.
     */
    void onPrioridadCalculada(SolicitudAcademica solicitud,
                               Usuario ejecutor,
                               String justificacion);
}
