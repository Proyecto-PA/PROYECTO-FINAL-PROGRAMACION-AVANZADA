package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.MotorReglasPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ResultadoPrioridad;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MotorReglasPrioridadTest {

    private MotorReglasPrioridad motor;

    @BeforeEach
    void setUp() {
        motor = new MotorReglasPrioridad();
    }

    @Test
    void solicitudConImpacto5YFechaProxima_debeDarCritica() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(5);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(1));
        solicitud.setTipo(TipoSolicitud.HOMOLOGACION);
        solicitud.setCanalOrigen(CanalOrigen.CSU);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getPrioridad()).isEqualTo(Prioridad.CRITICA);
        assertThat(resultado.getJustificacion()).isNotBlank();
    }

    @Test
    void solicitudConsultaConImpacto1SinFecha_debeDarBaja() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(2L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(1);
        solicitud.setFechaLimite(null);
        solicitud.setTipo(TipoSolicitud.CONSULTA_ACADEMICA);
        solicitud.setCanalOrigen(CanalOrigen.TELEFONO);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getPrioridad()).isEqualTo(Prioridad.BAJA);
    }

    @Test
    void solicitudConFechaEn5Dias_debeDarAlMenosAlta() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(3L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(4);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(5));
        solicitud.setTipo(TipoSolicitud.HOMOLOGACION);
        solicitud.setCanalOrigen(CanalOrigen.CSU);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getPrioridad()).isIn(Prioridad.ALTA, Prioridad.CRITICA);
    }

    @Test
    void justificacionDebeContenerDescripcionDeReglas() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(4L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(3);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(15));
        solicitud.setTipo(TipoSolicitud.REGISTRO_ASIGNATURAS);
        solicitud.setCanalOrigen(CanalOrigen.CORREO);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getJustificacion()).contains("Prioridad final calculada");
    }

    @Test
    void solicitudSinCamposOpcionales_noDebeArrojarExcepcion() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(5L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(null);
        solicitud.setFechaLimite(null);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getPrioridad()).isNotNull();
        assertThat(resultado.getJustificacion()).isNotBlank();
    }
}
