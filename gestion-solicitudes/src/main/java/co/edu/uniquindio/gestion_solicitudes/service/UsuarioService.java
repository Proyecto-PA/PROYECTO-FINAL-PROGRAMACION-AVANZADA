package co.edu.uniquindio.gestion_solicitudes.service;

import co.edu.uniquindio.gestion_solicitudes.dto.response.UsuarioResumenResponse;
import java.util.List;

public interface UsuarioService {
    List<UsuarioResumenResponse> listarDocentes();
}
