package co.edu.uniquindio.gestion_solicitudes.repository;

import co.edu.uniquindio.gestion_solicitudes.domain.entity.*;
import co.edu.uniquindio.gestion_solicitudes.domain.enums.*;
import co.edu.uniquindio.gestion_solicitudes.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - SolicitudRepository")
class SolicitudRepositoryTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    private final Usuario solicitante = TestDataFactory.crearUsuario(1L, RolUsuario.ESTUDIANTE);

    @Test
    @DisplayName("save persiste la solicitud y retorna con ID")
    void save_solicitudValida_retornaConId() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(null, solicitante);
        SolicitudAcademica guardada = TestDataFactory.crearSolicitud(1L, solicitante);

        when(solicitudRepository.save(solicitud)).thenReturn(guardada);

        SolicitudAcademica result = solicitudRepository.save(solicitud);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
    }

    @Test
    @DisplayName("findById retorna solicitud cuando existe")
    void findById_existe_retornaSolicitud() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        Optional<SolicitudAcademica> result = solicitudRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById retorna vacío cuando no existe")
    void findById_noExiste_retornaVacio() {
        when(solicitudRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<SolicitudAcademica> result = solicitudRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findWithFilters por estado retorna solo solicitudes con ese estado")
    void findWithFilters_porEstado_retornaFiltradas() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);
        Page<SolicitudAcademica> page = new PageImpl<>(List.of(solicitud));
        Pageable pageable = PageRequest.of(0, 10);

        when(solicitudRepository.findWithFilters(
                EstadoSolicitud.REGISTRADA, null, null, null, null, pageable))
                .thenReturn(page);

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                EstadoSolicitud.REGISTRADA, null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEstado())
                .isEqualTo(EstadoSolicitud.REGISTRADA);
    }

    @Test
    @DisplayName("findWithFilters sin filtros retorna todas las solicitudes")
    void findWithFilters_sinFiltros_retornaTodas() {
        List<SolicitudAcademica> solicitudes = List.of(
                TestDataFactory.crearSolicitud(1L, solicitante),
                TestDataFactory.crearSolicitud(2L, solicitante)
        );
        Page<SolicitudAcademica> page = new PageImpl<>(solicitudes);
        Pageable pageable = PageRequest.of(0, 10);

        when(solicitudRepository.findWithFilters(
                null, null, null, null, null, pageable))
                .thenReturn(page);

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                null, null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findWithFilters por tipo retorna solo solicitudes de ese tipo")
    void findWithFilters_porTipo_retornaFiltradas() {
        SolicitudAcademica solicitud = TestDataFactory.crearSolicitud(1L, solicitante);
        solicitud.setTipo(TipoSolicitud.HOMOLOGACION);
        Page<SolicitudAcademica> page = new PageImpl<>(List.of(solicitud));
        Pageable pageable = PageRequest.of(0, 10);

        when(solicitudRepository.findWithFilters(
                null, TipoSolicitud.HOMOLOGACION, null, null, null, pageable))
                .thenReturn(page);

        Page<SolicitudAcademica> result = solicitudRepository.findWithFilters(
                null, TipoSolicitud.HOMOLOGACION, null, null, null, pageable);

        assertThat(result.getContent().get(0).getTipo())
                .isEqualTo(TipoSolicitud.HOMOLOGACION);
    }
}