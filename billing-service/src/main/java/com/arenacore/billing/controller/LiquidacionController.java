package com.arenacore.billing.controller;

import com.arenacore.billing.dto.BillingDtos.*;
import com.arenacore.billing.service.BillingCoreService;
import com.arenacore.billing.service.IdempotencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class LiquidacionController {

    private final BillingCoreService billingService;
    private final IdempotencyService idempotencyService;

    public LiquidacionController(BillingCoreService billingService, IdempotencyService idempotencyService) {
        this.billingService = billingService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping("/internal/v1/liquidaciones")
    @PreAuthorize("hasAuthority('ROLE_SERVICE_INTERNAL')")
    public ResponseEntity<String> crearLiquidacion(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CrearLiquidacionRequest request) {
        String key = idempotencyService.requireKey(idempotencyKey);
        String endpoint = "POST:/internal/v1/liquidaciones";
        return idempotencyService.findReplay(key, endpoint, request)
                .orElseGet(() -> {
                    LiquidacionResponse result = billingService.crearLiquidacionConsolidada(request);
                    return idempotencyService.saveAndBuildResponse(key, endpoint, request, HttpStatus.CREATED, result);
                });
    }

    @GetMapping("/internal/v1/liquidaciones/{id}")
    @PreAuthorize("hasAuthority('ROLE_SERVICE_INTERNAL')")
    public ResponseEntity<LiquidacionResponse> obtenerInternal(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.obtenerLiquidacion(id));
    }

    @GetMapping("/api/billing/liquidaciones/{id}")
    public ResponseEntity<LiquidacionResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.obtenerLiquidacion(id));
    }

    @GetMapping("/api/billing/liquidaciones/sesion/{sesionJuegoId}")
    public ResponseEntity<LiquidacionResponse> obtenerPorSesion(@PathVariable UUID sesionJuegoId) {
        return ResponseEntity.ok(billingService.obtenerLiquidacionPorSesion(sesionJuegoId));
    }
}
