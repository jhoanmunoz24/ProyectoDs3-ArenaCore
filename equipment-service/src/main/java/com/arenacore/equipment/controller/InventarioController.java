package com.arenacore.equipment.controller;

import com.arenacore.equipment.dto.EquipmentDtos.*;
import com.arenacore.equipment.service.EquipmentCoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {

    private final EquipmentCoreService equipmentService;

    public InventarioController(EquipmentCoreService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping("/{implementoId}/ajustar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ImplementoResponse> ajustarStock(
            @PathVariable UUID implementoId,
            @Valid @RequestBody AjusteStockRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(equipmentService.ajustarStock(implementoId, request, adminId));
    }

    @GetMapping("/{implementoId}/kardex")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'OPERADOR')")
    public ResponseEntity<List<MovimientoInventarioResponse>> consultarKardex(@PathVariable UUID implementoId) {
        return ResponseEntity.ok(equipmentService.consultarKardex(implementoId));
    }
}
