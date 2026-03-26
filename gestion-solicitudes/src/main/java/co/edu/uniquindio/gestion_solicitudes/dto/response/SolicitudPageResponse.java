package co.edu.uniquindio.gestion_solicitudes.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class SolicitudPageResponse {
    private List<SolicitudResponse> contenido;
    private int paginaActual;
    private int totalPaginas;
    private long totalElementos;
}
