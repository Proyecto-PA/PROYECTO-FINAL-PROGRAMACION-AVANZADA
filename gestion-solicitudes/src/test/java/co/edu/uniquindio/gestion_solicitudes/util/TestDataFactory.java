package co.edu.uniquindio.gestion_solicitudes.util;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.dto.request.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.CanalOrigen;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.TipoSolicitud;

import co.edu.uniquindio.gestion_solicitudes.dto.response.SolicitudResponse;
import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;

import java.time.LocalDateTime;

public class TestDataFactory {

    /**
     * Convierte una entidad SolicitudAcademica a SolicitudResponse.
     * Usado en tests para mockear SolicitudFactory.toResponse().
     */
    public static SolicitudResponse crearResponseDesdeEntidad(SolicitudAcademica s) {
        return SolicitudResponse.builder()
                .id(s.getId())
                .tipo(s.getTipo())
                .estado(s.getEstado())
                .prioridad(s.getPrioridad())
                .justificacionPrioridad(s.getJustificacionPrioridad())
                .impactoAcademico(s.getImpactoAcademico())
                .descripcion(s.getDescripcion())
                .canalOrigen(s.getCanalOrigen())
                .fechaRegistro(s.getFechaRegistro())
                .fechaLimite(s.getFechaLimite())
                .solicitante(s.getSolicitante() != null ? UsuarioResumenResponse.builder()
                        .id(s.getSolicitante().getId())
                        .nombre(s.getSolicitante().getNombre())
                        .email(s.getSolicitante().getEmail())
                        .rol(s.getSolicitante().getRol())
                        .build() : null)
                .responsable(s.getResponsable() != null ? UsuarioResumenResponse.builder()
                        .id(s.getResponsable().getId())
                        .nombre(s.getResponsable().getNombre())
                        .email(s.getResponsable().getEmail())
                        .rol(s.getResponsable().getRol())
                        .build() : null)
                .build();
    }

    public static Usuario crearUsuario(Long id, RolUsuario rol){
        return Usuario.builder()
                .id(id)
                .nombre("Usuario Test")
                .email("test" + id + "@universidad.edu")
                .passwordHash("hashedPassword")
                .rol(rol)
                .activo(true)
                .build();
    }

    public static SolicitudAcademica crearSolicitud(Long id, Usuario solicitante){
        return SolicitudAcademica.builder()
                .id(id)
                .tipo(TipoSolicitud.REGISTRO_ASIGNATURAS)
                .descripcion("Descripción de prueba suficienctemente larga")
                .canalOrigen(CanalOrigen.CSU)
                .fechaRegistro(LocalDateTime.now())
                .fechaLimite(LocalDateTime.now().plusDays(10))
                .estado(EstadoSolicitud.REGISTRADA)
                .solicitante(solicitante)
                .impactoAcademico(3)
                .build();
    }

    public static SolicitudRequest crearSolicitudRequest(){
        SolicitudRequest request = new SolicitudRequest();
        request.setTipo(TipoSolicitud.REGISTRO_ASIGNATURAS);
        request.setDescripcion("Descripción de prueba suficientemente larga");
        request.setCanalOrigen(CanalOrigen.CSU);
        request.setFechaLimite(LocalDateTime.now().plusDays(10));
        return request;
    }

    public static ClasificarRequest crearClasificarRequest(TipoSolicitud tipo, Integer impacto, String obs) {
        ClasificarRequest request = new ClasificarRequest();
        request.setTipo(tipo);
        request.setImpactoAcademico(impacto);
        request.setObservacion(obs);
        return request;
    }

    public static CambiarEstadoRequest crearCambiarEstadoRequest(String observacion) {
        CambiarEstadoRequest request = new CambiarEstadoRequest();
        request.setObservacion(observacion);
        return request;
    }

    public static CerrarSolicitudRequest crearCerrarRequest(String observacion) {
        CerrarSolicitudRequest request = new CerrarSolicitudRequest();
        request.setObservacion(observacion);
        return request;
    }

    public static RegistroRequest crearRegistroRequest(String email, RolUsuario rol) {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Usuario Test");
        request.setEmail(email);
        request.setPassword("123456");
        request.setRol(rol);
        return request;
    }

    public static LoginRequest crearLoginRequest(String email) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("123456");
        return request;
    }
}
