package com.arenacore.equipment.service;

import com.arenacore.equipment.client.CourtsInternalClient;
import com.arenacore.equipment.dto.EquipmentDtos.RegistrarPrestamoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Orquesta validación HTTP con Courts Service FUERA de cualquier transacción JDBC (HT-05 / ADR-006).
 */
@Service
public class PrestamoOrchestratorService {

    private final IdempotencyService idempotencyService;
    private final CourtsInternalClient courtsClient;
    private final EquipmentCoreService coreService;

    public PrestamoOrchestratorService(
            IdempotencyService idempotencyService,
            CourtsInternalClient courtsClient,
            EquipmentCoreService coreService) {
        this.idempotencyService = idempotencyService;
        this.courtsClient = courtsClient;
        this.coreService = coreService;
    }

    public ResponseEntity<String> crearPrestamo(String rawKey, RegistrarPrestamoRequest request, UUID operadorId) {
        String key = idempotencyService.requireKey(rawKey);
        String endpoint = "POST:/api/v1/prestamos";

        Optional<ResponseEntity<String>> replay = idempotencyService.findReplay(key, endpoint, request);
        if (replay.isPresent()) {
            return replay.get();
        }

        // Llamada HTTP síncrona fuera de transacción local @Transactional
        courtsClient.validarSesionActiva(request.sesionJuegoId());

        return coreService.registrarPrestamoLocal(key, endpoint, request, operadorId);
    }
}
