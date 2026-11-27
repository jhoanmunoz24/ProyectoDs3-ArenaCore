package com.arenacore.equipment.controller;

import com.arenacore.equipment.dto.EquipmentDtos.*;
import com.arenacore.equipment.service.EquipmentCoreService;
import com.arenacore.equipment.service.PrestamoOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class PrestamoController {

    private final PrestamoOrchestratorService orchestratorService;
    private final EquipmentCoreService equipmentService;

    public PrestamoController(PrestamoOrchestratorService orchestratorService, EquipmentCoreService equipmentService) {
        this.orchestratorService = orchestratorService;
        this.equipmentService = equipmentService;
    }

    @PostMapping("/api/v1/prestamos")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<String> registrarPrestamo(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody RegistrarPrestamoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID operadorId = UUID.fromString(jwt.getSubject());
        return orchestratorService.crearPrestamo(idempotencyKey, request, operadorId);
    }

    @GetMapping("/api/v1/prestamos/sesion/{sesionId}")
    public ResponseEntity<List<PrestamoResponse>> listarPorSesion(@PathVariable UUID sesionId) {
        return ResponseEntity.ok(equipmentService.listarPrestamosPorSesion(sesionId));
    }

    @GetMapping("/internal/v1/prestamos/sesion/{sesionId}/cargos")
    @PreAuthorize("hasAuthority('ROLE_SERVICE_INTERNAL')")
    public ResponseEntity<CargosSesionResponse> obtenerCargosInternal(@PathVariable UUID sesionId) {
        return ResponseEntity.ok(equipmentService.obtenerCargosPorSesion(sesionId));
    }
}
