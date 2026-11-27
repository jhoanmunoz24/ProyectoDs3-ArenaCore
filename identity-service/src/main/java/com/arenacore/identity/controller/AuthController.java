package com.arenacore.identity.controller;

import com.arenacore.identity.dto.IdentityDtos.*;
import com.arenacore.identity.service.IdentityManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IdentityManagementService identityService;

    public AuthController(IdentityManagementService identityService) {
        this.identityService = identityService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(identityService.login(request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(identityService.registerClient(request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/service-token")
    public ResponseEntity<ServiceTokenResponse> serviceToken(
            @Valid @RequestBody ServiceTokenRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(identityService.issueServiceToken(request, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(identityService.getUser(UUID.fromString(jwt.getSubject())));
    }
}
