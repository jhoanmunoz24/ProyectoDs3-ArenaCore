package com.arenacore.courts.controller;

import com.arenacore.courts.dto.CourtsDtos.*;
import com.arenacore.courts.service.CourtsCoreService;
import com.arenacore.courts.service.SesionOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final CourtsCoreService courtsService;
    private final SesionOrchestratorService orchestratorService;

    public ReservaController(CourtsCoreService courtsService, SesionOrchestratorService orchestratorService) {
        this.courtsService = courtsService;
        this.orchestratorService = orchestratorService;
    }

    @PostMapping
    public ResponseEntity<String> crearReserva(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CrearReservaRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        return courtsService.crearReserva(idempotencyKey, request, actorId);
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listarReservas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(courtsService.listarReservas(fecha));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelarReserva(
            @PathVariable UUID id,
            @Valid @RequestBody CancelarReservaRequest request) {
        return ResponseEntity.ok(orchestratorService.cancelarReserva(id, request));
    }
}
