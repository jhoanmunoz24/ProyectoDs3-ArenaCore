package com.arenacore.billing.controller;

import com.arenacore.billing.dto.BillingDtos.*;
import com.arenacore.billing.service.BillingCoreService;
import com.arenacore.billing.service.IdempotencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing/cajas")
public class CajaController {

    private final BillingCoreService billingService;
    private final IdempotencyService idempotencyService;

    public CajaController(BillingCoreService billingService, IdempotencyService idempotencyService) {
        this.billingService = billingService;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<CajaResponse>> listarCajas() {
        return ResponseEntity.ok(billingService.listarCajas());
    }

    @PostMapping("/sesiones")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<String> abrirSesion(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody AbrirSesionCajaRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String key = idempotencyService.requireKey(idempotencyKey);
        String endpoint = "POST:/api/billing/cajas/sesiones";
        UUID operadorId = UUID.fromString(jwt.getSubject());
        return idempotencyService.findReplay(key, endpoint, request)
                .orElseGet(() -> {
                    SesionCajaResponse res = billingService.abrirSesionCaja(request, operadorId);
                    return idempotencyService.saveAndBuildResponse(key, endpoint, request, HttpStatus.CREATED, res);
                });
    }

    @PostMapping("/sesiones/{id}/cerrar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<SesionCajaResponse> cerrarSesion(
            @PathVariable UUID id,
            @Valid @RequestBody CerrarSesionCajaRequest request) {
        return ResponseEntity.ok(billingService.cerrarSesionCaja(id, request));
    }

    @GetMapping("/sesiones/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<SesionCajaResponse> consultarArqueo(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.consultarArqueoCaja(id));
    }
}
