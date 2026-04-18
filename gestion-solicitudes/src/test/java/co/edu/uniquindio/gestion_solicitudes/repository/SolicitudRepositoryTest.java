package co.edu.uniquindio.gestion_solicitudes.repository;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.Usuario;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de integración - SolicitudRepository")
class SolicitudRepositoryTest {

    @Autowired private SolicitudRepository solicitudRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario solicitante;
    private Usuario responsable;

    @BeforeEach
    void setUp() {
        solicitante = usuarioRepository.save(Usuario.builder()
                .nombre("Estudiante Test")
                .email("est@universidad.edu")
                .passwordHash("hash")
                .rol(RolUsuario.ESTUDIANTE)
                .activo(true)
                .build());

        responsable = usuarioRepository.save(Usuario.builder()
                .nombre("Docente Test")
                .email("doc@universidad.edu")
                .passwordHash("hash")
                .rol(RolUsuario.DOCENTE)
                .activo(true)
                .build());
    }

    // ---- helpers ----

    private SolicitudAcademica buildSolicitud(TipoSolicitud tipo,
                                              EstadoSolicitud estado,
                                              Prioridad prioridad) {
        return SolicitudAcademica.builder()
                .tipo(tipo)
                .descripcion("Descripción de prueba suficientemente larga para pasar validación")
                .canalOrigen(CanalOrigen.CSU)
                .fechaRegistro(LocalDateTime.now())
                .estado(estado)
                .prioridad(prioridad)
                .solicitante(solicitante)
                .build();
    }

    // ---- save / findById ----

    @Test
    @DisplayName("save persiste la solicitud y genera ID")
    void save_solicitudValida_persisteYGeneraId() {
        SolicitudAcademica solicitud = buildSolicitud(
                TipoSolicitud.HOMOLOGACION,
                EstadoSolicitud.REGISTRADA,
                null);

        SolicitudAcademica guardada = solicitudRepository.save(solicitud);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(guardada.getTipo()).isEqualTo(TipoSolicitud.HOMOLOGACION);
        assertThat(guardada.getSolicitante().getId()).isEqualTo(solicitante.getId());
    }

    @Test
    @DisplayName("findById retorna la solicitud cuando existe")
    void findById_existe_retornaSolicitud() {
        SolicitudAcademica guardada = solicitudRepository.save(
                buildSolicitud(TipoSolicitud.HOMOLOGACION, EstadoSolicitud.REGISTRADA, null));

        Optional<SolicitudAcademica> result = solicitudRepository.findById(guardada.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(guardada.getId());
    }

    @Test
    @DisplayName("findById retorna vacío cuando no existe")
    void findById_noExiste_retornaVacio() {
        Optional<SolicitudAcademica> result = solicitudRepository.findById(9999L);

        assertThat(result).isEmpty();
    }

    // ---- findWithFilters ----

    @Test
    @DisplayName("findWithFilters sin filtros retorna todas las solicitudes")
    void findWithFilters_sinFiltros_retornaTodas() {
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.HOMOLOGACION, EstadoSolicitud.REGISTRADA, null));
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.CONSULTA_ACADEMICA, EstadoSolicitud.CLASIFICADA, Prioridad.MEDIA));

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findWithFilters por estado retorna solo las de ese estado")
    void findWithFilters_porEstado_retornaFiltradas() {
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.HOMOLOGACION, EstadoSolicitud.REGISTRADA, null));
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.CONSULTA_ACADEMICA, EstadoSolicitud.CLASIFICADA, Prioridad.MEDIA));

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                EstadoSolicitud.REGISTRADA, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
    }

    @Test
    @DisplayName("findWithFilters por tipo retorna solo las de ese tipo")
    void findWithFilters_porTipo_retornaFiltradas() {
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.HOMOLOGACION, EstadoSolicitud.REGISTRADA, null));
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.CONSULTA_ACADEMICA, EstadoSolicitud.REGISTRADA, null));

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                null, TipoSolicitud.HOMOLOGACION, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTipo()).isEqualTo(TipoSolicitud.HOMOLOGACION);
    }

    @Test
    @DisplayName("findWithFilters por prioridad retorna solo las de esa prioridad")
    void findWithFilters_porPrioridad_retornaFiltradas() {
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.HOMOLOGACION, EstadoSolicitud.CLASIFICADA, Prioridad.ALTA));
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.CONSULTA_ACADEMICA, EstadoSolicitud.CLASIFICADA, Prioridad.BAJA));

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                null, null, Prioridad.ALTA, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getPrioridad()).isEqualTo(Prioridad.ALTA);
    }

    @Test
    @DisplayName("findWithFilters por responsableId retorna solo las asignadas a ese responsable")
    void findWithFilters_porResponsable_retornaFiltradas() {
        SolicitudAcademica conResponsable = buildSolicitud(
                TipoSolicitud.HOMOLOGACION, EstadoSolicitud.CLASIFICADA, null);
        conResponsable.setResponsable(responsable);
        solicitudRepository.save(conResponsable);

        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.CONSULTA_ACADEMICA, EstadoSolicitud.REGISTRADA, null));

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                null, null, null, responsable.getId(), null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getResponsable().getId())
                .isEqualTo(responsable.getId());
    }

    @Test
    @DisplayName("findWithFilters por solicitanteId retorna solo las de ese solicitante")
    void findWithFilters_porSolicitante_retornaFiltradas() {
        // Segundo solicitante
        Usuario otro = usuarioRepository.save(Usuario.builder()
                .nombre("Otro Estudiante")
                .email("otro@universidad.edu")
                .passwordHash("hash")
                .rol(RolUsuario.ESTUDIANTE)
                .activo(true)
                .build());

        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.HOMOLOGACION, EstadoSolicitud.REGISTRADA, null));

        SolicitudAcademica deOtro = SolicitudAcademica.builder()
                .tipo(TipoSolicitud.CONSULTA_ACADEMICA)
                .descripcion("Descripción de prueba suficientemente larga para pasar validación")
                .canalOrigen(CanalOrigen.CORREO)
                .fechaRegistro(LocalDateTime.now())
                .estado(EstadoSolicitud.REGISTRADA)
                .solicitante(otro)
                .build();
        solicitudRepository.save(deOtro);

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                null, null, null, null, solicitante.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getSolicitante().getId())
                .isEqualTo(solicitante.getId());
    }

    @Test
    @DisplayName("findWithFilters combina múltiples filtros correctamente")
    void findWithFilters_multiplesFiltros_retornaCorrectamente() {
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.HOMOLOGACION, EstadoSolicitud.CLASIFICADA, Prioridad.ALTA));
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.HOMOLOGACION, EstadoSolicitud.REGISTRADA, null));
        solicitudRepository.save(buildSolicitud(
                TipoSolicitud.CONSULTA_ACADEMICA, EstadoSolicitud.CLASIFICADA, Prioridad.ALTA));

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                EstadoSolicitud.CLASIFICADA, TipoSolicitud.HOMOLOGACION,
                Prioridad.ALTA, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTipo()).isEqualTo(TipoSolicitud.HOMOLOGACION);
        assertThat(result.getContent().get(0).getEstado()).isEqualTo(EstadoSolicitud.CLASIFICADA);
    }

    @Test
    @DisplayName("findWithFilters con paginación retorna el tamaño correcto por página")
    void findWithFilters_paginacion_retornaTamanioCorrecto() {
        for (int i = 0; i < 5; i++) {
            solicitudRepository.save(buildSolicitud(
                    TipoSolicitud.HOMOLOGACION, EstadoSolicitud.REGISTRADA, null));
        }

        Page<SolicitudAcademica> paginaUno = solicitudRepository.findWithFilters(
                null, null, null, null, null, PageRequest.of(0, 3));
        Page<SolicitudAcademica> paginaDos = solicitudRepository.findWithFilters(
                null, null, null, null, null, PageRequest.of(1, 3));

        assertThat(paginaUno.getContent()).hasSize(3);
        assertThat(paginaDos.getContent()).hasSize(2);
        assertThat(paginaUno.getTotalElements()).isEqualTo(5);
        assertThat(paginaUno.getTotalPages()).isEqualTo(2);
    }
}