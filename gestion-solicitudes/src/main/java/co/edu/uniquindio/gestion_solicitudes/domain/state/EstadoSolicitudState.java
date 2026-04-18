package co.edu.uniquindio.gestion_solicitudes.domain.state;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;

/**
 * Patrón State: cada implementación encapsula el comportamiento permitido desde
 * ese estado. La solicitud delega en su estado actual.
 */
public interface EstadoSolicitudState {
    void clasificar(SolicitudAcademica solicitud, Usuario ejecutor, Object payload);
    void iniciarAtencion(SolicitudAcademica solicitud, Usuario ejecutor, String observacion);
    void marcarAtendida(SolicitudAcademica solicitud, Usuario ejecutor, String observacion);
    void cerrar(SolicitudAcademica solicitud, Usuario ejecutor, String observacion);
    void cancelar(SolicitudAcademica solicitud, Usuario ejecutor, String observacion);
    String getNombre();
}
