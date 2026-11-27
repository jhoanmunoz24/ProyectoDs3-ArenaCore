package com.arenacore.billing.controller;

import com.arenacore.billing.dto.BillingDtos.ComprobanteResponse;
import com.arenacore.billing.service.BillingCoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/billing/comprobantes")
public class ComprobanteController {

    private final BillingCoreService billingService;

    public ComprobanteController(BillingCoreService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComprobanteResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.obtenerComprobante(id));
    }

    @GetMapping("/liquidacion/{liquidacionId}")
    public ResponseEntity<ComprobanteResponse> obtenerPorLiquidacion(@PathVariable UUID liquidacionId) {
        return ResponseEntity.ok(billingService.obtenerComprobantePorLiquidacion(liquidacionId));
    }
}
