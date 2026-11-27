package com.arenacore.courts.client;

import com.arenacore.courts.dto.CourtsDtos.CrearLiquidacionInternalRequest;
import com.arenacore.courts.dto.CourtsDtos.LiquidacionInternalResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class BillingInternalClient {

    private final RestClient billingRestClient;
    private final ServiceTokenClient serviceTokenClient;

    public BillingInternalClient(
            @Value("${arenacore.clients.billing-url}") String billingUrl,
            @Value("${arenacore.clients.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${arenacore.clients.read-timeout-ms:5000}") int readTimeoutMs,
            ServiceTokenClient serviceTokenClient) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(connectTimeoutMs);
        rf.setReadTimeout(readTimeoutMs);
        this.billingRestClient = RestClient.builder().baseUrl(billingUrl).requestFactory(rf).build();
        this.serviceTokenClient = serviceTokenClient;
    }

    public LiquidacionInternalResponse crearLiquidacion(CrearLiquidacionInternalRequest request, String idempotencyKey) {
        try {
            String token = serviceTokenClient.getServiceToken();
            LiquidacionInternalResponse response = billingRestClient.post()
                    .uri("/internal/v1/liquidaciones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> {
                        h.setBearerAuth(token);
                        h.set("Idempotency-Key", idempotencyKey);
                    })
                    .body(request)
                    .retrieve()
                    .body(LiquidacionInternalResponse.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Billing Service devolvió liquidación vacía");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error creando liquidación en Billing Service: HTTP " + ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Billing Service no disponible para generar liquidación");
        }
    }

    public LiquidacionInternalResponse obtenerEstadoLiquidacion(UUID liquidacionId) {
        try {
            String token = serviceTokenClient.getServiceToken();
            LiquidacionInternalResponse response = billingRestClient.get()
                    .uri("/internal/v1/liquidaciones/{id}", liquidacionId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .body(LiquidacionInternalResponse.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Billing Service devolvió respuesta vacía");
            }
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Liquidación no encontrada en Billing Service");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error consultando estado de liquidación en Billing Service: HTTP " + ex.getStatusCode().value());
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Billing Service no disponible para verificar pago");
        }
    }
}
