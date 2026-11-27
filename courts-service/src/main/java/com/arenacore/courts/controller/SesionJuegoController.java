package com.arenacore.courts.controller;

import com.arenacore.courts.dto.CourtsDtos.*;
import com.arenacore.courts.service.CourtsCoreService;
import com.arenacore.courts.service.SesionOrchestratorService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class SesionJuegoController {

    private final CourtsCoreService courtsService;
    private final SesionOrchestratorService orchestratorService;

    public SesionJuegoController(CourtsCoreService courtsService, SesionOrchestratorService orchestratorService) {
        this.courtsService = courtsService;
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/api/v1/sesiones-juego/checkin")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<String> checkIn(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID operadorId = UUID.fromString(jwt.getSubject());
        return courtsService.registrarCheckIn(idempotencyKey, request, operadorId);
    }

    @GetMapping("/api/v1/sesiones-juego")
    public ResponseEntity<List<SesionJuegoResponse>> listar(
            @RequestParam(defaultValue = "false") boolean activas) {
        return ResponseEntity.ok(courtsService.listarSesiones(activas));
    }

    @GetMapping("/api/v1/sesiones-juego/{id}")
    public ResponseEntity<SesionJuegoResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(courtsService.obtenerSesion(id));
    }

    @PostMapping({"/api/v1/sesiones-juego/{id}/solicitar-liquidacion", "/api/v1/sesiones-juego/{id}/liquidar"})
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<LiquidacionInternalResponse> solicitarLiquidacion(
            @PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(orchestratorService.solicitarLiquidacion(id, idempotencyKey, actorId));
    }

    @PostMapping("/api/v1/sesiones-juego/{id}/cerrar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<SesionJuegoResponse> cerrar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(orchestratorService.cerrarSesion(id, actorId));
    }

    @GetMapping("/internal/v1/sesiones-juego/{id}")
    @PreAuthorize("hasAuthority('ROLE_SERVICE_INTERNAL')")
    public ResponseEntity<SesionJuegoInternalResponse> obtenerInternal(@PathVariable UUID id) {
        SesionJuegoResponse s = courtsService.obtenerSesion(id);
        return ResponseEntity.ok(new SesionJuegoInternalResponse(s.id(), s.estado(), s.canchaNombre()));
    }
}
