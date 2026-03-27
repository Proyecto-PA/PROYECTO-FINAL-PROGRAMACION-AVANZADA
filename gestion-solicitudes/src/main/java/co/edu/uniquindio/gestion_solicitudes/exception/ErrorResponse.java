package co.edu.uniquindio.gestion_solicitudes.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String path;
}
