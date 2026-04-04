package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
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

        MotorReglasPrioridad.ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.prioridad()).isEqualTo(Prioridad.CRITICA);
        assertThat(resultado.justificacion()).isNotBlank();
    }

    @Test
    void solicitudConsultaConImpacto1SinFecha_debeDarBaja() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(2L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(1);
        solicitud.setFechaLimite(null);
        solicitud.setTipo(TipoSolicitud.CONSULTA_ACADEMICA);
        solicitud.setCanalOrigen(CanalOrigen.TELEFONO);

        MotorReglasPrioridad.ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.prioridad()).isEqualTo(Prioridad.BAJA);
    }

    @Test
    void solicitudConFechaEn5Dias_debeDarAlMenosAlta() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(3L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(4);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(5));
        solicitud.setTipo(TipoSolicitud.HOMOLOGACION);
        solicitud.setCanalOrigen(CanalOrigen.CSU);

        MotorReglasPrioridad.ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.prioridad()).isIn(Prioridad.ALTA, Prioridad.CRITICA);
    }

    @Test
    void justificacionDebeContenerDescripcionDeReglas() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(4L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(3);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(15));
        solicitud.setTipo(TipoSolicitud.REGISTRO_ASIGNATURAS);
        solicitud.setCanalOrigen(CanalOrigen.CORREO);

        MotorReglasPrioridad.ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.justificacion()).contains("Prioridad final calculada");
    }

    @Test
    void solicitudSinCamposOpcionales_noDebeArrojarExcepcion() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(5L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(null);
        solicitud.setFechaLimite(null);

        MotorReglasPrioridad.ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.prioridad()).isNotNull();
        assertThat(resultado.justificacion()).isNotBlank();
    }
}
