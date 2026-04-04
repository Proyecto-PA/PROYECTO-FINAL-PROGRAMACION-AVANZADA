package co.edu.uniquindio.gestion_solicitudes.domain.rules.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ReglaPrioridad;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ReglaPorFechaLimite implements ReglaPrioridad {

    @Override
    public Prioridad evaluar(SolicitudAcademica solicitud) {
        if (solicitud.getFechaLimite() == null) return null;

        long diasRestantes = ChronoUnit.DAYS.between(LocalDateTime.now(), solicitud.getFechaLimite());

        if (diasRestantes <= 2)  return Prioridad.CRITICA;
        if (diasRestantes <= 7)  return Prioridad.ALTA;
        if (diasRestantes <= 30) return Prioridad.MEDIA;
        return Prioridad.BAJA;
    }

    @Override
    public String getDescripcion(SolicitudAcademica solicitud) {
        if (solicitud.getFechaLimite() == null) return "Sin fecha límite definida.";
        long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), solicitud.getFechaLimite());
        return "Fecha límite en " + dias + " día(s).";
    }

    @Override
    public int getPeso() {
        return 4;
    }
}
