package co.edu.uniquindio.gestion_solicitudes.domain.model;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialSolicitud {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudAcademica solicitudAcademica;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id")
    private Usuario responsable;

    @Column(name = "fecha_acción", nullable = false)
    private LocalDateTime fecha_accion;

    @Column(name = "accion_realizada", nullable = false)
    private String accionRealizada;

    @Column(length = 500)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoSolicitud estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name  = "estado_nuevo")
    private EstadoSolicitud estadoNuevo;

}
