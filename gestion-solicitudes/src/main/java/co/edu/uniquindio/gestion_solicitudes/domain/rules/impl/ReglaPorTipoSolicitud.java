package co.edu.uniquindio.gestion_solicitudes.domain.rules.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ReglaPrioridad;

public class ReglaPorTipoSolicitud implements ReglaPrioridad {

    @Override
    public Prioridad evaluar(SolicitudAcademica solicitud) {
        TipoSolicitud tipo = solicitud.getTipo();
        if (tipo == null) return null;

        return switch (tipo) {
            case HOMOLOGACION, CANCELACION_ASIGNATURAS -> Prioridad.ALTA;
            case REGISTRO_ASIGNATURAS, SOLICITUD_CUPOS  -> Prioridad.MEDIA;
            case CONSULTA_ACADEMICA, OTRO               -> Prioridad.BAJA;
        };
    }

    @Override
    public String getDescripcion(SolicitudAcademica solicitud) {
        if (solicitud.getTipo() == null) return "Tipo de solicitud no especificado.";
        return "Tipo de solicitud: " + solicitud.getTipo().name() + ".";
    }

    @Override
    public int getPeso() {
        return 2;
    }
}
