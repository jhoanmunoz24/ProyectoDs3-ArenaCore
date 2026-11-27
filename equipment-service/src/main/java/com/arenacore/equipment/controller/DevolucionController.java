package com.arenacore.equipment.controller;

import com.arenacore.equipment.dto.EquipmentDtos.*;
import com.arenacore.equipment.service.EquipmentCoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prestamos")
public class DevolucionController {

    private final EquipmentCoreService equipmentService;

    public DevolucionController(EquipmentCoreService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping("/{id}/devolver")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<PrestamoResponse> devolver(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarDevolucionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID operadorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(equipmentService.registrarDevolucion(id, request, operadorId));
    }
}
