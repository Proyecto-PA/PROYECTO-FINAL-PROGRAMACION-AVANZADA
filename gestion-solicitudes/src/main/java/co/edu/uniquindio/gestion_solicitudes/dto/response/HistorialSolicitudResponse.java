package co.edu.uniquindio.gestion_solicitudes.dto.response;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HistorialSolicitudResponse {
    private Long id;
    private LocalDateTime fechaAccion;
    private String accionRealizada;
    private UsuarioResumenResponse usuarioResponsable;
    private String observaciones;
    private EstadoSolicitud estadoAnterior;
    private EstadoSolicitud estadoNuevo;
}