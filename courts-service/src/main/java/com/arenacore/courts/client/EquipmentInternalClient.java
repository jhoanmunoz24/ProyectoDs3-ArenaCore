package com.arenacore.courts.client;

import com.arenacore.courts.dto.CourtsDtos.CargosSesionInternalResponse;
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
public class EquipmentInternalClient {

    private final RestClient equipmentRestClient;
    private final ServiceTokenClient serviceTokenClient;

    public EquipmentInternalClient(
            @Value("${arenacore.clients.equipment-url}") String equipmentUrl,
            @Value("${arenacore.clients.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${arenacore.clients.read-timeout-ms:5000}") int readTimeoutMs,
            ServiceTokenClient serviceTokenClient) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(connectTimeoutMs);
        rf.setReadTimeout(readTimeoutMs);
        this.equipmentRestClient = RestClient.builder().baseUrl(equipmentUrl).requestFactory(rf).build();
        this.serviceTokenClient = serviceTokenClient;
    }

    public CargosSesionInternalResponse obtenerCargosSesion(UUID sesionJuegoId) {
        try {
            String token = serviceTokenClient.getServiceToken();
            CargosSesionInternalResponse response = equipmentRestClient.get()
                    .uri("/internal/v1/prestamos/sesion/{id}/cargos", sesionJuegoId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .body(CargosSesionInternalResponse.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta vacía de Equipment Service");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error consultando cargos en Equipment Service: HTTP " + ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Equipment Service no disponible para consultar préstamos");
        }
    }
}
