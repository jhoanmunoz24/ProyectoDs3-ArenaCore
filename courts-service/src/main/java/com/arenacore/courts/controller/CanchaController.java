package com.arenacore.courts.controller;

import com.arenacore.courts.domain.MantenimientoCancha;
import com.arenacore.courts.dto.CourtsDtos.*;
import com.arenacore.courts.service.CourtsCoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/canchas")
public class CanchaController {

    private final CourtsCoreService courtsService;

    public CanchaController(CourtsCoreService courtsService) {
        this.courtsService = courtsService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CanchaResponse> crear(@Valid @RequestBody CanchaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courtsService.crearCancha(request));
    }

    @GetMapping
    public ResponseEntity<List<CanchaResponse>> listar(@RequestParam(required = false) UUID sedeId) {
        return ResponseEntity.ok(courtsService.listarCanchas(sedeId));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CanchaResponse> actualizarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CanchaEstadoRequest request) {
        return ResponseEntity.ok(courtsService.actualizarEstadoCancha(id, request));
    }

    @PostMapping("/{id}/mantenimientos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MantenimientoCancha> programarMantenimiento(
            @PathVariable UUID id,
            @Valid @RequestBody MantenimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courtsService.programarMantenimiento(id, request));
    }
}
