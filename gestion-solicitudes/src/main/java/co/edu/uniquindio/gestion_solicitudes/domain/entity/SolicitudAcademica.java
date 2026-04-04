package co.edu.uniquindio.gestion_solicitudes.domain.entity;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes_academicas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAcademica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSolicitud tipo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen", nullable = false)
    private CanalOrigen canalOrigen;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    @Column(name = "justificacion_prioridad", length = 1000)
    private String justificacionPrioridad;

    @Column(name = "impacto_academico")
    private Integer impactoAcademico; // 1 a 5

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HistorialSolicitud> historial = new ArrayList<>();

    //Métodos
    public void clasificarSolicitud(TipoSolicitud tipoSolicitud){
        this.tipo = tipoSolicitud;
    }

    public void asignarResponsable(Usuario usuario){
        this.responsable = usuario;
    }

    public void cambiarEstado(EstadoSolicitud nuevoEstado){
        this.estado = nuevoEstado;
    }

    public boolean estaCerrada(){
        return this.estado == EstadoSolicitud.CERRADA || this.estado == EstadoSolicitud.CANCELADA;
    }
}
