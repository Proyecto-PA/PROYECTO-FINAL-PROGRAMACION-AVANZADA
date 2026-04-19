package co.edu.uniquindio.gestion_solicitudes.domain.validator;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
        import co.edu.uniquindio.gestion_solicitudes.domain.rules.MotorReglasPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.ResultadoPrioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorCanalOrigen;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorFechaLimite;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorImpactoAcademico;
import co.edu.uniquindio.gestion_solicitudes.domain.rules.impl.ReglaPorTipoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitarios - MotorReglasPrioridad")
class MotorReglasPrioridadTest {

    private MotorReglasPrioridad motor;

    @BeforeEach
    void setUp() {
        motor = new MotorReglasPrioridad(List.of(
                new ReglaPorFechaLimite(),
                new ReglaPorImpactoAcademico(),
                new ReglaPorTipoSolicitud(),
                new ReglaPorCanalOrigen()
        ));
    }

    @Test
    @DisplayName("Impacto 5 y fecha próxima debe dar CRITICA")
    void solicitudConImpacto5YFechaProxima_debeDarCritica() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(
                1L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(5);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(1));
        solicitud.setTipo(TipoSolicitud.HOMOLOGACION);
        solicitud.setCanalOrigen(CanalOrigen.CSU);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getPrioridad()).isEqualTo(Prioridad.CRITICA);
        assertThat(resultado.getJustificacion()).isNotBlank();
    }

    @Test
    @DisplayName("Consulta con impacto 1 y sin fecha debe dar BAJA")
    void solicitudConsultaConImpacto1SinFecha_debeDarBaja() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(
                2L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(1);
        solicitud.setFechaLimite(null);
        solicitud.setTipo(TipoSolicitud.CONSULTA_ACADEMICA);
        solicitud.setCanalOrigen(CanalOrigen.TELEFONO);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getPrioridad()).isEqualTo(Prioridad.BAJA);
    }

    @Test
    @DisplayName("Fecha en 5 días con impacto 4 debe dar ALTA o CRITICA")
    void solicitudConFechaEn5Dias_debeDarAlMenosAlta() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(
                3L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(4);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(5));
        solicitud.setTipo(TipoSolicitud.HOMOLOGACION);
        solicitud.setCanalOrigen(CanalOrigen.CSU);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getPrioridad()).isIn(Prioridad.ALTA, Prioridad.CRITICA);
    }

    @Test
    @DisplayName("Justificación debe contener 'Prioridad final calculada'")
    void justificacionDebeContenerDescripcionDeReglas() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(
                4L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(3);
        solicitud.setFechaLimite(LocalDateTime.now().plusDays(15));
        solicitud.setTipo(TipoSolicitud.REGISTRO_ASIGNATURAS);
        solicitud.setCanalOrigen(CanalOrigen.CORREO);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getJustificacion()).contains("Prioridad final calculada");
    }

    @Test
    @DisplayName("Sin campos opcionales no debe lanzar excepción")
    void solicitudSinCamposOpcionales_noDebeArrojarExcepcion() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(
                5L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(null);
        solicitud.setFechaLimite(null);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        assertThat(resultado.getPrioridad()).isNotNull();
        assertThat(resultado.getJustificacion()).isNotBlank();
    }

    @Test
    @DisplayName("Solo con tipo y canal (sin impacto ni fecha) retorna prioridad válida")
    void solicitudSoloConTipoYCanal_retornaPrioridadValida() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(
                6L, TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE));
        solicitud.setImpactoAcademico(null);
        solicitud.setFechaLimite(null);
        solicitud.setTipo(TipoSolicitud.CANCELACION_ASIGNATURAS);
        solicitud.setCanalOrigen(CanalOrigen.PRESENCIAL);

        ResultadoPrioridad resultado = motor.calcular(solicitud);

        // Canal PRESENCIAL → ALTA, Tipo CANCELACION → ALTA → promedio ALTA
        assertThat(resultado.getPrioridad()).isIn(Prioridad.ALTA, Prioridad.MEDIA);
    }
}