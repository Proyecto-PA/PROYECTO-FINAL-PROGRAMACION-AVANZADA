package co.edu.uniquindio.gestion_solicitudes.service.ia;

import co.edu.uniquindio.gestion_solicitudes.config.IaProperties;
import co.edu.uniquindio.gestion_solicitudes.domain.entity.SolicitudAcademica;
import co.edu.uniquindio.gestion_solicitudes.dto.response.SugerenciaIAResponse;
import co.edu.uniquindio.gestion_solicitudes.exception.ServicioIANoDisponibleException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsistenteIA {

    private final IaProperties iaProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Genera una sugerencia de clasificación, prioridad y resumen para la solicitud dada.
     * Si la IA no está disponible, lanza ServicioIANoDisponibleException (HTTP 503).
     */
    public SugerenciaIAResponse generarSugerencia(SolicitudAcademica solicitudAcademica){
        if (!iaProperties.getHabilitada()){
            throw new ServicioIANoDisponibleException("El asistente de IA está deshabilitada en la configuración.");
        }
        if (iaProperties.getApi().getKey() == null || iaProperties.getApi().getKey().isBlank()){
            throw new ServicioIANoDisponibleException("No se ha configurado la clave de API para el asistente de IA.");
        }

        try{
            String prompt = construirPrompt(solicitudAcademica);
            String respuestaRaw = llamarAPI(prompt);
            return parsearRespuesta(respuestaRaw);
        } catch (ServicioIANoDisponibleException e){
            throw e;
        } catch (Exception e){
            log.error("Error al contactar el servicio de IA: {}", e.getMessage(), e);
            throw new ServicioIANoDisponibleException("El asistente de IA no responde en este momento. Puede continuar operando sin él.");
        }
    }


    // Construcción del prompt
    private String construirPrompt(SolicitudAcademica solicitudAcademica) {
        String historialTexto = solicitudAcademica.getHistorial().stream()
                .map(h -> String.format("[%s] %s - %s",
                        h.getFechaAccion(), h.getAccionRealizada(), h.getObservaciones() != null ? h.getObservaciones() : "Sin observaciones")).collect(Collectors.joining("\n"));
        return """
                Eres un asistente académico experto en gestión de solicitudes academicas.
                Analiza la siguiente solicitud académica y proporciona sugerencias.
                
                SOLICITUD:
                - Descripción: %s
                - Tipo actual: %s
                - Canal de origen: %s
                - Impacto académico (1-5): %s
                - Fecha límite: %s
                - Estado actual: %s
                
                HISTORIAL:
                %s
                
                Response ÚNICAMENTE en formato JSON con esta estructura exacta (sin texto adicional):
                {
                    "tipoSugerido" : "<uno de: REGISTRO_ASIGNATURAS, HOMOLOGACION, CANCELACION_ASIGNATURAS, SOLICITUD_CUPOS, CONSULTA_ACADEMICA, OTRO>",
                    "prioridadSugerida" : "<uno de: CRITICA, ALTA, MEDIA, BAJA>",
                    "resumen": "<resumen breve del caso en 2-3 oraciones>"
                }
                """.formatted(
                solicitudAcademica.getDescripcion(),
                solicitudAcademica.getTipo() != null ? solicitudAcademica.getTipo().name() : "NO CLASIFICADA",
                solicitudAcademica.getCanalOrigen() != null ? solicitudAcademica.getCanalOrigen().name() : "DESCONOCIDO",
                solicitudAcademica.getImpactoAcademico() != null ? solicitudAcademica.getImpactoAcademico() : "no especificado",
                solicitudAcademica.getFechaLimite() != null ? solicitudAcademica.getFechaLimite().toString() : "sin fecha limite",
                solicitudAcademica.getEstado().name(),
                historialTexto.isBlank() ? "Sin historial previo." : historialTexto
        );
    }

    // Llamado HTTP a la API
    private String llamarAPI(String prompt){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(iaProperties.getApi().getKey());

        Map<String, Object> body = Map.of(
    "model", iaProperties.getApi().getModel(),
    "messages", List.of(Map.of("role", "user", "content", prompt)),
    "temperature", 0.3,
    "max_tokens", 512
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try{
            ResponseEntity<String> response = restTemplate.exchange(iaProperties.getApi().getUrl(), HttpMethod.POST, entity, String.class);
            log.info("Respuesta Groq status: {}", response.getStatusCode());
            return response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e){
            log.error("Error HTTP Groq - Status: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServicioIANoDisponibleException("El asistente de IA no pudo procesar la solicitud. Intente nuevamente más tarde.");
        }
    }

    // Parseo de la respuesta
    private SugerenciaIAResponse parsearRespuesta(String respuestaRaw) {
        try{
            JsonNode root = objectMapper.readTree(respuestaRaw);

            // Estructura de respuesta OpenIa/Groq
            String textoIA = root
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            textoIA = textoIA.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode datos = objectMapper.readTree(textoIA);

            return SugerenciaIAResponse.builder()
                    .tipoSugerido(datos.path("tipoSugerido").asText("OTRO"))
                    .prioridadSugerida(datos.path("prioridadSugerida").asText("MEDIA"))
                    .resumen(datos.path("resumen").asText("Sin resumen disponible."))
                    .confirmada(false)
                    .fechaSugerencia(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error parseando respuesta Groq: {}", e.getMessage());
            throw new ServicioIANoDisponibleException("La IA devolvió una respuesta inválida");
        }
    }
}
