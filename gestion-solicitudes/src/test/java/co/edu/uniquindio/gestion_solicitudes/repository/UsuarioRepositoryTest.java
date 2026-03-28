package co.edu.uniquindio.gestion_solicitudes.repository;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.RolUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - UsuarioRepository")
public class UsuarioRepositoryTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("findByEmail retorna usuario cuando existe")
    void findByEmail_existe_retornaUsuario() {
        Usuario usuario = Usuario.builder()
                .id(1L).nombre("Test").email("test@universidad.edu")
                .passwordHash("hash").rol(RolUsuario.ESTUDIANTE).activo(true)
                .build();

        when(usuarioRepository.findByEmail("test@universidad.edu"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> result = usuarioRepository.findByEmail("test@universidad.edu");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@universidad.edu");
        assertThat(result.get().getRol()).isEqualTo(RolUsuario.ESTUDIANTE);
    }

    @Test
    @DisplayName("findByEmail retorna vacío cuando no existe")
    void findByEmail_noExiste_retornaVacio() {
        when(usuarioRepository.findByEmail("noexiste@universidad.edu"))
                .thenReturn(Optional.empty());

        Optional<Usuario> result = usuarioRepository.findByEmail("noexiste@universidad.edu");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save persiste el usuario y retorna con ID")
    void save_usuarioValido_retornaConId() {
        Usuario usuario = Usuario.builder()
                .nombre("Test").email("test@universidad.edu")
                .passwordHash("hash").rol(RolUsuario.DOCENTE).activo(true)
                .build();
        Usuario guardado = Usuario.builder()
                .id(1L).nombre("Test").email("test@universidad.edu")
                .passwordHash("hash").rol(RolUsuario.DOCENTE).activo(true)
                .build();

        when(usuarioRepository.save(usuario)).thenReturn(guardado);

        Usuario result = usuarioRepository.save(usuario);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRol()).isEqualTo(RolUsuario.DOCENTE);
    }

    @Test
    @DisplayName("findById retorna usuario cuando existe")
    void findById_existe_retornaUsuario() {
        Usuario usuario = Usuario.builder()
                .id(1L).nombre("Test").email("test@universidad.edu")
                .passwordHash("hash").rol(RolUsuario.ADMINISTRATIVO).activo(true)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> result = usuarioRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById retorna vacío cuando no existe")
    void findById_noExiste_retornaVacio() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Usuario> result = usuarioRepository.findById(999L);

        assertThat(result).isEmpty();
    }
}
