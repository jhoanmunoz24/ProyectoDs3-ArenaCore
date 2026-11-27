package com.arenacore.courts.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@Component
public class ServiceTokenClient {

    private final RestClient identityRestClient;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    public ServiceTokenClient(
            @Value("${arenacore.clients.identity-url}") String identityUrl,
            @Value("${arenacore.clients.service-client-id}") String clientId,
            @Value("${arenacore.clients.service-client-secret}") String clientSecret,
            @Value("${arenacore.clients.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${arenacore.clients.read-timeout-ms:5000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(connectTimeoutMs);
        rf.setReadTimeout(readTimeoutMs);
        this.identityRestClient = RestClient.builder().baseUrl(identityUrl).requestFactory(rf).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public synchronized String getServiceToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(30))) {
            return cachedToken;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = identityRestClient.post()
                    .uri("/api/auth/service-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("clientId", clientId, "clientSecret", clientSecret))
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.get("accessToken") == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Identity no devolvió token de servicio");
            }
            this.cachedToken = body.get("accessToken").toString();
            long expiresIn = body.get("expiresIn") instanceof Number n ? n.longValue() : 3600L;
            this.expiresAt = Instant.now().plusSeconds(expiresIn);
            return this.cachedToken;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo autenticar servicio contra Identity Service: " + ex.getMessage());
        }
    }
}
