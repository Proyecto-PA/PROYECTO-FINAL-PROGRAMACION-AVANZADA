package co.edu.uniquindio.gestion_solicitudes.dto.response;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.CanalOrigen;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class SolicitudResponse {
    private Long id;
    private TipoSolicitud tipoSolicitud;
    private String descripcion;
    private CanalOrigen canalOrigen;
    private LocalDateTime fechaRegistro;
    private EstadoSolicitud estadoSolicitud;
    private Prioridad prioridad;
    private Integer impactoAcademico;
    private UsuarioResumenResponse solicitante;
    private UsuarioResumenResponse responsable;
}
