package co.edu.uniquindio.gestion_solicitudes.service.impl;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.dto.request.ConfirmarSugerenciaRequest;
import co.edu.uniquindio.gestion_solicitudes.dto.response.*;
import co.edu.uniquindio.gestion_solicitudes.exception.ResourceNotFoundException;
import co.edu.uniquindio.gestion_solicitudes.repository.*;
import co.edu.uniquindio.gestion_solicitudes.service.AsistenteIA;
import co.edu.uniquindio.gestion_solicitudes.service.IAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class IAServiceImpl implements  IAService {
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialRepository historialRepository;
    private final AsistenteIA asistenteIA;


    //Generar sugerencia
    @Override
    @Transactional(readOnly = true)
    public SugerenciaIAResponse generarSugerencia(Long solicitudId) {
        SolicitudAcademica solicitudAcademica = buscarSolicitud(solicitudId);
        return asistenteIA.generarSugerencia(solicitudAcademica);
    }

    // Confirmar  / Descartar sugerencia
    @Override
    @Transactional
    public SolicitudResponse confirmarSugerencia(Long solicitudId, ConfirmarSugerenciaRequest request, Long usuarioId) {
        SolicitudAcademica solicitudAcademica = buscarSolicitud(solicitudId);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con id " + usuarioId));

        //Si el usuario descarta la sugerencia, no se modifica nada
        if (Boolean.FALSE.equals(request.getAplicar())) {
            log.info("Sugerencia IA descartada para solicitud {}", solicitudId);
            registrarHistorial(solicitudAcademica, usuario, "Sugerencia IA descartada por el usuario.", null, null);
            return toResponse(solicitudAcademica);
        }

        // Aplicar tipo sugerido
        TipoSolicitud tipoFinal = request.getTipoAjustado() != null ? request.getTipoAjustado() : solicitudAcademica.getTipo();

        // Aplicar prioridad sugerida
        Prioridad prioridadFinal = request.getPrioridadAjustada() != null ? request.getPrioridadAjustada() : solicitudAcademica.getPrioridad();

        StringBuilder observacion = new StringBuilder("Sugerencia IA aplicada.");
        if (tipoFinal != null && tipoFinal != solicitudAcademica.getTipo()) {
            solicitudAcademica.clasificarSolicitud(tipoFinal);
            observacion.append("Tipo actualizado a ").append(tipoFinal.name()).append(".");
        }
        if (prioridadFinal != null && prioridadFinal != solicitudAcademica.getPrioridad()) {
            solicitudAcademica.setPrioridad(prioridadFinal);
            solicitudAcademica.setJustificacionPrioridad("Prioridad " + prioridadFinal.name() + " establecida con asistente IA.");
            observacion.append(" Prioridad actualizada a ").append(prioridadFinal.name()).append(".");
        }

        solicitudAcademica = solicitudRepository.save(solicitudAcademica);
        registrarHistorial(solicitudAcademica, usuario, "Sugerencia IA confirmada y aplicada. ", observacion.toString(), null);
        return toResponse(solicitudAcademica);
    }

    // Helpers
    private SolicitudAcademica buscarSolicitud(Long id){
        return solicitudRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe una solicitud con id " + id));
    }

    private void registrarHistorial(SolicitudAcademica solicitudAcademica, Usuario usuario, String accion, String observaciones, String extra){
        historialRepository.save(HistorialSolicitud.builder()
                .solicitud(solicitudAcademica)
                .fechaAccion(LocalDateTime.now())
                .accionRealizada(accion)
                .usuarioResponsable(usuario)
                .estadoAnterior(solicitudAcademica.getEstado())
                .estadoNuevo(solicitudAcademica.getEstado())
                .observaciones(observaciones)
                .build());
    }

    private SolicitudResponse toResponse(SolicitudAcademica solicitudAcademica){
        return SolicitudResponse.builder()
                .id(solicitudAcademica.getId())
                .tipo(solicitudAcademica.getTipo())
                .descripcion(solicitudAcademica.getDescripcion())
                .canalOrigen(solicitudAcademica.getCanalOrigen())
                .fechaRegistro(solicitudAcademica.getFechaRegistro())
                .fechaLimite(solicitudAcademica.getFechaLimite())
                .estado(solicitudAcademica.getEstado())
                .prioridad(solicitudAcademica.getPrioridad())
                .justificacionPrioridad(solicitudAcademica.getJustificacionPrioridad())
                .impactoAcademico(solicitudAcademica.getImpactoAcademico())
                .solicitante(toUsuarioResumen(solicitudAcademica.getSolicitante()))
                .responsable(solicitudAcademica.getResponsable() != null ? toUsuarioResumen(solicitudAcademica.getResponsable()) : null)
                .build();
    }

    private UsuarioResumenResponse toUsuarioResumen(Usuario usuario){
        return UsuarioResumenResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }
}
