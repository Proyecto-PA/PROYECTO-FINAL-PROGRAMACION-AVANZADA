package co.edu.uniquindio.gestion_solicitudes.repository;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRol(RolUsuario rol);
}
