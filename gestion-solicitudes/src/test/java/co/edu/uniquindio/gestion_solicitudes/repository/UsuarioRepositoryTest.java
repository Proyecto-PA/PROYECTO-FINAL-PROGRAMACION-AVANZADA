package co.edu.uniquindio.gestion_solicitudes.repository;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de integración - UsuarioRepository")
class UsuarioRepositoryTest {

    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario buildUsuario(String email, RolUsuario rol) {
        return Usuario.builder()
                .nombre("Usuario Test")
                .email(email)
                .passwordHash("hashedPassword")
                .rol(rol)
                .activo(true)
                .build();
    }

    // ---- save / findById ----

    @Test
    @DisplayName("save persiste el usuario y genera ID")
    void save_usuarioValido_persisteYGeneraId() {
        Usuario guardado = usuarioRepository.save(
                buildUsuario("est@universidad.edu", RolUsuario.ESTUDIANTE));

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getEmail()).isEqualTo("est@universidad.edu");
        assertThat(guardado.getRol()).isEqualTo(RolUsuario.ESTUDIANTE);
        assertThat(guardado.getActivo()).isTrue();
    }

    @Test
    @DisplayName("findById retorna el usuario cuando existe")
    void findById_existe_retornaUsuario() {
        Usuario guardado = usuarioRepository.save(
                buildUsuario("doc@universidad.edu", RolUsuario.DOCENTE));

        Optional<Usuario> result = usuarioRepository.findById(guardado.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getRol()).isEqualTo(RolUsuario.DOCENTE);
    }

    @Test
    @DisplayName("findById retorna vacío cuando no existe")
    void findById_noExiste_retornaVacio() {
        Optional<Usuario> result = usuarioRepository.findById(9999L);

        assertThat(result).isEmpty();
    }

    // --- findByEmail ----

    @Test
    @DisplayName("findByEmail retorna el usuario cuando el email existe")
    void findByEmail_existe_retornaUsuario() {
        usuarioRepository.save(buildUsuario("admin@universidad.edu", RolUsuario.ADMINISTRATIVO));

        Optional<Usuario> result = usuarioRepository.findByEmail("admin@universidad.edu");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@universidad.edu");
        assertThat(result.get().getRol()).isEqualTo(RolUsuario.ADMINISTRATIVO);
    }

    @Test
    @DisplayName("findByEmail retorna vacío cuando el email no existe")
    void findByEmail_noExiste_retornaVacio() {
        Optional<Usuario> result = usuarioRepository.findByEmail("noexiste@universidad.edu");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("email es único — no se pueden guardar dos usuarios con el mismo email")
    void save_emailDuplicado_lanzaExcepcion() {
        usuarioRepository.save(buildUsuario("duplicado@universidad.edu", RolUsuario.ESTUDIANTE));

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> usuarioRepository.saveAndFlush(
                        buildUsuario("duplicado@universidad.edu", RolUsuario.DOCENTE)));
    }

    @Test
    @DisplayName("activo por defecto es true al guardar")
    void save_activoPorDefecto_esTrue() {
        Usuario usuario = Usuario.builder()
                .nombre("Test")
                .email("nuevo@universidad.edu")
                .passwordHash("hash")
                .rol(RolUsuario.ESTUDIANTE)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        assertThat(guardado.getActivo()).isTrue();
    }

    @Test
    @DisplayName("usuario inactivo se persiste correctamente")
    void save_usuarioInactivo_persiste() {
        Usuario usuario = buildUsuario("inactivo@universidad.edu", RolUsuario.ESTUDIANTE);
        usuario.setActivo(false);

        Usuario guardado = usuarioRepository.save(usuario);

        assertThat(guardado.getActivo()).isFalse();
    }

    @Test
    @DisplayName("findByEmail es case-sensitive")
    void findByEmail_caseSensitive_noEncuentraConMayusculas() {
        usuarioRepository.save(buildUsuario("caso@universidad.edu", RolUsuario.ESTUDIANTE));

        Optional<Usuario> result = usuarioRepository.findByEmail("CASO@UNIVERSIDAD.EDU");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("persiste los tres roles correctamente")
    void save_tresRoles_persistenCorrectamente() {
        usuarioRepository.save(buildUsuario("est@universidad.edu", RolUsuario.ESTUDIANTE));
        usuarioRepository.save(buildUsuario("doc@universidad.edu", RolUsuario.DOCENTE));
        usuarioRepository.save(buildUsuario("adm@universidad.edu", RolUsuario.ADMINISTRATIVO));

        assertThat(usuarioRepository.findByEmail("est@universidad.edu").get().getRol())
                .isEqualTo(RolUsuario.ESTUDIANTE);
        assertThat(usuarioRepository.findByEmail("doc@universidad.edu").get().getRol())
                .isEqualTo(RolUsuario.DOCENTE);
        assertThat(usuarioRepository.findByEmail("adm@universidad.edu").get().getRol())
                .isEqualTo(RolUsuario.ADMINISTRATIVO);
    }
}