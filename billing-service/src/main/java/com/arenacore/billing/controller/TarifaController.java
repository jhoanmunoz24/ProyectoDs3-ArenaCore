package com.arenacore.billing.controller;

import com.arenacore.billing.dto.BillingDtos.*;
import com.arenacore.billing.service.BillingCoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/billing/tarifas")
public class TarifaController {

    private final BillingCoreService billingService;

    public TarifaController(BillingCoreService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/vigentes")
    public ResponseEntity<TarifasVigentesResponse> obtenerVigentes() {
        return ResponseEntity.ok(billingService.obtenerTarifasVigentes());
    }

    @PutMapping("/vigentes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TarifasVigentesResponse> actualizarVigentes(
            @Valid @RequestBody ActualizarTarifasRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(billingService.actualizarTarifasVigentes(request, adminId));
    }
}
