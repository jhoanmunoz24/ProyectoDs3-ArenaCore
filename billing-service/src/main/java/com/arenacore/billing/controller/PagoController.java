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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/billing/pagos")
public class PagoController {

    private final BillingCoreService billingService;
    private final IdempotencyService idempotencyService;

    public PagoController(BillingCoreService billingService, IdempotencyService idempotencyService) {
        this.billingService = billingService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping("/{id}/resolver")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<String> resolverPago(
            @PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ResolverPagoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String key = idempotencyService.requireKey(idempotencyKey);
        String endpoint = "POST:/api/billing/pagos/" + id + "/resolver";
        UUID operadorId = UUID.fromString(jwt.getSubject());
        return idempotencyService.findReplay(key, endpoint, request)
                .orElseGet(() -> {
                    PagoResueltoResponse res = billingService.resolverPago(id, request, operadorId);
                    return idempotencyService.saveAndBuildResponse(key, endpoint, request, HttpStatus.OK, res);
                });
    }
}
