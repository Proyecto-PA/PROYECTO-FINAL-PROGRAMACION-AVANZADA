package co.edu.uniquindio.gestion_solicitudes.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExeptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request){
        return ResponseEntity.status((HttpStatus.NOT_FOUND)).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(404)
                        .error("Not Found")
                        .mensaje(ex.getMessage())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request){
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status((HttpStatus.BAD_REQUEST)).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(400)
                        .error("Bad Request")
                        .mensaje(errores)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(409)
                        .error("Conflict")
                        .mensaje(ex.getMessage())
                        .path(request.getRequestURI())
                        .build()
        );
    }
}
