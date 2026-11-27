package com.arenacore.courts.client;

import com.arenacore.courts.dto.CourtsDtos.AutorizacionInternalResponse;
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
public class IdentityInternalClient {

    private final RestClient identityRestClient;
    private final ServiceTokenClient serviceTokenClient;

    public IdentityInternalClient(
            @Value("${arenacore.clients.identity-url}") String identityUrl,
            @Value("${arenacore.clients.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${arenacore.clients.read-timeout-ms:5000}") int readTimeoutMs,
            ServiceTokenClient serviceTokenClient) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(connectTimeoutMs);
        rf.setReadTimeout(readTimeoutMs);
        this.identityRestClient = RestClient.builder().baseUrl(identityUrl).requestFactory(rf).build();
        this.serviceTokenClient = serviceTokenClient;
    }

    public boolean esAutorizacionAprobada(UUID autorizacionId, UUID reservaId) {
        try {
            String token = serviceTokenClient.getServiceToken();
            AutorizacionInternalResponse auth = identityRestClient.get()
                    .uri("/internal/v1/autorizaciones/{id}", autorizacionId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .body(AutorizacionInternalResponse.class);
            return auth != null
                    && "APROBADA".equals(auth.estado())
                    && reservaId.equals(auth.referenciaExternaId());
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return false;
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error validando autorización en Identity Service");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Identity Service no disponible para validar autorización excepcional");
        }
    }
}
