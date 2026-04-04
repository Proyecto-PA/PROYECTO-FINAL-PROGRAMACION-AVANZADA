package co.edu.uniquindio.gestion_solicitudes.domain.rules.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.CanalOrigen;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ReglaPrioridad;

public class ReglaPorCanalOrigen implements ReglaPrioridad {

    @Override
    public Prioridad evaluar(SolicitudAcademica solicitud) {
        CanalOrigen canal = solicitud.getCanalOrigen();
        if (canal == null) return null;

        return switch (canal) {
            case CSU, PRESENCIAL -> Prioridad.ALTA;
            case CORREO, SAC     -> Prioridad.MEDIA;
            case TELEFONO        -> Prioridad.BAJA;
        };
    }

    @Override
    public String getDescripcion(SolicitudAcademica solicitud) {
        if (solicitud.getCanalOrigen() == null) return "Canal de origen no especificado.";
        return "Canal de origen: " + solicitud.getCanalOrigen().name() + ".";
    }

    @Override
    public int getPeso() {
        return 1;
    }
}
