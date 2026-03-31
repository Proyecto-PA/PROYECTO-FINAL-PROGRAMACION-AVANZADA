package co.edu.uniquindio.gestion_solicitudes.domain.validator;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests unitarios - ValidadorTransicionEstado")

public class ValidadorTransicionEstadoTest {
    @Test
    @DisplayName("REGISTRADA → CLASIFICADA es válida")
    void registradaAClasificada_esValida() {
        assertThatNoException().isThrownBy(() ->
            ValidadorTransicionEstado.validarOLanzar(
                EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA));
    }

    @Test
    @DisplayName("REGISTRADA → CANCELADA es válida")
    void registradaACancelada_esValida() {
        assertThatNoException().isThrownBy(() ->
            ValidadorTransicionEstado.validarOLanzar(
                EstadoSolicitud.REGISTRADA, EstadoSolicitud.CANCELADA));
    }

    @Test
    @DisplayName("CLASIFICADA → EN_ATENCION es válida")
    void clasificadaAEnAtencion_esValida() {
        assertThatNoException().isThrownBy(() ->
            ValidadorTransicionEstado.validarOLanzar(
                EstadoSolicitud.CLASIFICADA, EstadoSolicitud.EN_ATENCION));
    }

    @Test
    @DisplayName("EN_ATENCION → ATENDIDA es válida")
    void enAtencionAAtendida_esValida() {
        assertThatNoException().isThrownBy(() ->
            ValidadorTransicionEstado.validarOLanzar(
                EstadoSolicitud.EN_ATENCION, EstadoSolicitud.ATENDIDA));
    }

    @Test
    @DisplayName("ATENDIDA → CERRADA es válida")
    void atendidaACerrada_esValida() {
        assertThatNoException().isThrownBy(() ->
            ValidadorTransicionEstado.validarOLanzar(
                EstadoSolicitud.ATENDIDA, EstadoSolicitud.CERRADA));
    }

    @Test
    @DisplayName("REGISTRADA → ATENDIDA es inválida (salto de estados)")
    void registradaAAtendida_esInvalida() {
        assertThatThrownBy(() ->
            ValidadorTransicionEstado.validarOLanzar(
                EstadoSolicitud.REGISTRADA, EstadoSolicitud.ATENDIDA))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("REGISTRADA")
            .hasMessageContaining("ATENDIDA");
    }

    @Test
    @DisplayName("EN_ATENCION → REGISTRADA es inválida (retroceso)")
    void enAtencionARegistrada_esInvalida() {
        assertThatThrownBy(() ->
            ValidadorTransicionEstado.validarOLanzar(
                EstadoSolicitud.EN_ATENCION, EstadoSolicitud.REGISTRADA))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("CERRADA → cualquier estado es inválida (estado terminal)")
    void cerradaACualquiera_esInvalida() {
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            assertThatThrownBy(() ->
                ValidadorTransicionEstado.validarOLanzar(
                    EstadoSolicitud.CERRADA, estado))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("CANCELADA → cualquier estado es inválida (estado terminal)")
    void canceladaACualquiera_esInvalida() {
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            assertThatThrownBy(() ->
                ValidadorTransicionEstado.validarOLanzar(
                    EstadoSolicitud.CANCELADA, estado))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("CLASIFICADA → CLASIFICADA es inválida")
    void clasificadaAClasificada_esInvalida() {
        assertThatThrownBy(() ->
            ValidadorTransicionEstado.validarOLanzar(
                EstadoSolicitud.CLASIFICADA, EstadoSolicitud.CLASIFICADA))
            .isInstanceOf(IllegalStateException.class);
    }
}
