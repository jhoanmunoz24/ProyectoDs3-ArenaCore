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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final IdentityManagementService identityService;

    public UsuarioController(IdentityManagementService identityService) {
        this.identityService = identityService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(identityService.createUser(request, actorId, httpRequest.getRemoteAddr()));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(identityService.listUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(identityService.getUser(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(identityService.updateUserStatus(id, request, actorId, httpRequest.getRemoteAddr()));
    }
}
