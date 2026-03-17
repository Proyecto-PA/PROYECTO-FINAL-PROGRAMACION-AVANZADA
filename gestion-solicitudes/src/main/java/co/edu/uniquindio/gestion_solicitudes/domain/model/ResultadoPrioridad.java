package co.edu.uniquindio.gestion_solicitudes.domain.model;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.Prioridad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resultados_prioridad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoPrioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudAcademica solicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridad prioridad;

    @Column(length = 1000)
    private String justificacion;

    @Column(name = "regla_aplicada")
    private String reglaAplicada;

    @Column(name = "fecha_evaluacion")
    private LocalDateTime fechaEvaluacion;
}
