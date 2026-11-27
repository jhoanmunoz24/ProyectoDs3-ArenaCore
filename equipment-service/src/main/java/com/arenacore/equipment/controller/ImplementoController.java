package com.arenacore.equipment.controller;

import com.arenacore.equipment.dto.EquipmentDtos.*;
import com.arenacore.equipment.service.EquipmentCoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/implementos")
public class ImplementoController {

    private final EquipmentCoreService equipmentService;

    public ImplementoController(EquipmentCoreService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ImplementoResponse> crear(
            @Valid @RequestBody ImplementoCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipmentService.crearImplemento(request, adminId));
    }

    @GetMapping
    public ResponseEntity<List<ImplementoResponse>> listarTodos() {
        return ResponseEntity.ok(equipmentService.listarImplementos(false));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<ImplementoResponse>> listarDisponibles() {
        return ResponseEntity.ok(equipmentService.listarImplementos(true));
    }
}
