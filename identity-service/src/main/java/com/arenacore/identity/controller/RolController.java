package com.arenacore.identity.controller;

import com.arenacore.identity.dto.IdentityDtos.*;
import com.arenacore.identity.service.IdentityManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class RolController {

    private final IdentityManagementService identityService;

    public RolController(IdentityManagementService identityService) {
        this.identityService = identityService;
    }

    @GetMapping("/api/roles")
    public ResponseEntity<List<RoleResponse>> listRoles() {
        return ResponseEntity.ok(identityService.listRoles());
    }

    @PutMapping("/api/users/{id}/roles")
    public ResponseEntity<UserResponse> assignRoles(
            @PathVariable UUID id,
            @Valid @RequestBody RoleAssignmentRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(identityService.assignRoles(id, request, actorId, httpRequest.getRemoteAddr()));
    }
}
