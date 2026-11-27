package com.arenacore.courts.service;

import com.arenacore.courts.domain.IdempotencyRecord;
import com.arenacore.courts.repository.IdempotencyRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public String requireKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El encabezado Idempotency-Key es obligatorio");
        }
        return idempotencyKey.trim();
    }

    @Transactional(readOnly = true)
    public Optional<ResponseEntity<String>> findReplay(String key, String endpoint, Object requestBody) {
        String hash = hashPayload(requestBody);
        return repository.findByIdempotencyKeyAndEndpoint(key, endpoint)
                .map(existing -> {
                    if (!existing.getRequestHash().equals(hash)) {
                        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                                "Idempotency-Key reutilizada con un cuerpo de petición diferente");
                    }
                    return ResponseEntity.status(existing.getResponseStatus())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(existing.getResponseBody());
                });
    }

    public <T> ResponseEntity<String> saveAndBuildResponse(
            String key, String endpoint, Object requestBody, HttpStatus status, T responsePayload) {
        try {
            String json = objectMapper.writeValueAsString(responsePayload);
            IdempotencyRecord record = new IdempotencyRecord();
            record.setId(UUID.randomUUID());
            record.setIdempotencyKey(key);
            record.setEndpoint(endpoint);
            record.setRequestHash(hashPayload(requestBody));
            record.setResponseStatus(status.value());
            record.setResponseBody(json);
            repository.save(record);
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error serializando respuesta idempotente", e);
        }
    }

    private String hashPayload(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(json.getBytes(StandardCharsets.UTF_8)));
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error calculando hash de idempotencia", e);
        }
    }
}
