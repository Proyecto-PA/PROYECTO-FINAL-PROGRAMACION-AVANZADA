package co.edu.uniquindio.gestion_solicitudes.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String mensaje){
        super(mensaje);
    }
}
