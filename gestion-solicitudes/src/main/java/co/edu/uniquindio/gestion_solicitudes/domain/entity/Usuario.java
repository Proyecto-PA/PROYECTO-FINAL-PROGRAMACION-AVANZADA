package co.edu.uniquindio.gestion_solicitudes.domain.entity;

import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    @Column(nullable = false)
    private Boolean activo = true;

    public boolean estaActivo(){
        return Boolean.TRUE.equals(this.activo);
    }

    public boolean tieneRol(RolUsuario rolEsperado){
        return this.rol == rolEsperado;
    }
}
