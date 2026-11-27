package com.arenacore.identity.controller;

import com.arenacore.identity.dto.IdentityDtos.*;
import com.arenacore.identity.service.IdentityManagementService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AutorizacionController {

    private final IdentityManagementService identityService;

    public AutorizacionController(IdentityManagementService identityService) {
        this.identityService = identityService;
    }

    @PostMapping("/api/autorizaciones")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<AutorizacionResponse> crear(
            @Valid @RequestBody AutorizacionCreateRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(identityService.createAutorizacion(request, userId, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/api/autorizaciones/{id}/aprobar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AutorizacionResponse> aprobar(
            @PathVariable UUID id,
            @RequestBody(required = false) AutorizacionResolveRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        String obs = request != null ? request.observacionResolucion() : null;
        return ResponseEntity.ok(identityService.resolveAutorizacion(id, true, obs, adminId, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/api/autorizaciones/{id}/rechazar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AutorizacionResponse> rechazar(
            @PathVariable UUID id,
            @RequestBody(required = false) AutorizacionResolveRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        String obs = request != null ? request.observacionResolucion() : null;
        return ResponseEntity.ok(identityService.resolveAutorizacion(id, false, obs, adminId, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/api/autorizaciones")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<AutorizacionResponse>> listar(@RequestParam(required = false) String estado) {
        return ResponseEntity.ok(identityService.listAutorizaciones(estado));
    }

    @GetMapping("/internal/v1/autorizaciones/{id}")
    @PreAuthorize("hasAuthority('ROLE_SERVICE_INTERNAL')")
    public ResponseEntity<AutorizacionResponse> getInternal(@PathVariable UUID id) {
        return ResponseEntity.ok(identityService.getAutorizacion(id));
    }
}
