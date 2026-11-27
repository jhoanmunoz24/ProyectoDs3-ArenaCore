package com.arenacore.equipment.client;

import com.arenacore.equipment.dto.EquipmentDtos.SesionJuegoEstadoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class CourtsInternalClient {

    private final RestClient courtsRestClient;
    private final ServiceTokenClient serviceTokenClient;

    public CourtsInternalClient(
            @Value("${arenacore.clients.courts-url}") String courtsUrl,
            @Value("${arenacore.clients.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${arenacore.clients.read-timeout-ms:5000}") int readTimeoutMs,
            ServiceTokenClient serviceTokenClient) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(connectTimeoutMs);
        rf.setReadTimeout(readTimeoutMs);
        this.courtsRestClient = RestClient.builder().baseUrl(courtsUrl).requestFactory(rf).build();
        this.serviceTokenClient = serviceTokenClient;
    }

    public void validarSesionActiva(UUID sesionJuegoId) {
        try {
            String token = serviceTokenClient.getServiceToken();
            SesionJuegoEstadoResponse sesion = courtsRestClient.get()
                    .uri("/internal/v1/sesiones-juego/{id}", sesionJuegoId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .body(SesionJuegoEstadoResponse.class);
            if (sesion == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta vacía desde Courts Service");
            }
            if (!"ACTIVA".equals(sesion.estado()) && !"LIQUIDACION_SOLICITADA".equals(sesion.estado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La sesión de juego " + sesionJuegoId + " no está ACTIVA (estado: " + sesion.estado() + ")");
            }
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La sesión de juego " + sesionJuegoId + " no existe en Courts Service");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error validando sesión en Courts Service: HTTP " + ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Courts Service no disponible para validar sesión de juego");
        }
    }
}
