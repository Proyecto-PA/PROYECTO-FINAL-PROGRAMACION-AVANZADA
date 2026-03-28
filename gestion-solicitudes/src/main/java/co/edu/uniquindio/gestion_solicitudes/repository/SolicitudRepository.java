package co.edu.uniquindio.gestion_solicitudes.repository;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitudRepository extends JpaRepository<SolicitudAcademica, Long> {

    @Query("""
    SELECT s FROM SolicitudAcademica s
    WHERE (:estado IS NULL OR s.estado = :estado)
      AND (:tipo IS NULL OR s.tipo = :tipo)
      AND (:prioridad IS NULL OR s.prioridad = :prioridad)
      AND (:responsableId IS NULL OR s.responsable.id = :responsableId)
      AND (:solicitanteId IS NULL OR s.solicitante.id = :solicitanteId)
""")
    Page<SolicitudAcademica> findWithFilters(
            @Param("estado") EstadoSolicitud estado,
            @Param("tipo") TipoSolicitud tipo,
            @Param("prioridad") Prioridad prioridad,
            @Param("responsableId") Long responsableId,
            @Param("solicitanteId") Long solicitanteId,
            Pageable pageable
    );
}
