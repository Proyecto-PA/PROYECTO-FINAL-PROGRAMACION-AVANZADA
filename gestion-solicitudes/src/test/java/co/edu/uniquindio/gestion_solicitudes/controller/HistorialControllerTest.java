package co.edu.uniquindio.gestion_solicitudes.controller;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import co.edu.uniquindio.gestion_solicitudes.dto.response.HistorialSolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.service.HistorialService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test unitarios - HistorialController")
public class HistorialControllerTest {

    @Mock private HistorialService historialService;
    @InjectMocks private HistorialController historialController;

    private HistorialSolicitudResponse buildResponse(Long id, String accion, EstadoSolicitud anterior, EstadoSolicitud nuevo){
        return HistorialSolicitudResponse.builder()
                .id(id)
                .accionRealizada(accion)
                .fechaAccion(LocalDateTime.now())
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .usuarioResponsable(UsuarioResumenResponse.builder()
                        .id(1L).nombre("Estudiante Test")
                        .email("est@universidad@edu")
                        .rol(RolUsuario.ESTUDIANTE)
                        .build())
                .build();
    }

    @Test
    @DisplayName("consultar - solciitud existente retorna 200 con historial")
    void consultar_solicitudExistente_retorna200(){
        List<HistorialSolicitudResponse> historial = List.of(buildResponse(1L, "Solicitud Registrada", null, EstadoSolicitud.REGISTRADA),
                buildResponse(2L, "Cambio: REGISTRADA -> CLASIFICADA",
                        EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA)
        );

        when(historialService.consultarPorSolicitud(1L)).thenReturn(historial);
        ResponseEntity<List<HistorialSolicitudResponse>> response = historialController.consultar(1L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
        verify(historialService).consultarPorSolicitud(1L);
    }

    @Test
    @DisplayName("consultar - solicitud inexistente propaga excepción del service")
    void consultar_solicitudInexistente_propagaExcepcion() {
        when(historialService.consultarPorSolicitud(999L))
                .thenThrow(new ResourceNotFoundException(
                        "No existe una solicitud con id 999"));

        assertThatThrownBy(() -> historialController.consultar(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("consultar - historial vacío retorna 200 con lista vacía")
    void consultar_sinEntradas_retornaListaVacia() {
        when(historialService.consultarPorSolicitud(1L)).thenReturn(List.of());

        ResponseEntity<List<HistorialSolicitudResponse>> response =
                historialController.consultar(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("consultar - delega completamente en el service")
    void consultar_delegaEnService() {
        when(historialService.consultarPorSolicitud(1L)).thenReturn(List.of());

        historialController.consultar(1L);

        verify(historialService, times(1)).consultarPorSolicitud(1L);
        verifyNoMoreInteractions(historialService);
    }
}
