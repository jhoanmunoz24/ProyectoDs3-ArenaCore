package com.arenacore.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class IdentityDtos {

    private IdentityDtos() {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record RegisterRequest(
            @NotBlank @Size(max = 150) String nombreCompleto,
            @NotBlank @Email String email,
            @Size(max = 30) String telefono,
            @NotBlank @Size(min = 6, max = 100) String password
    ) {}

    public record ServiceTokenRequest(
            @NotBlank String clientId,
            @NotBlank String clientSecret
    ) {}

    public record ServiceTokenResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            String serviceName
    ) {}

    public record UserCreateRequest(
            @NotBlank @Size(max = 150) String nombreCompleto,
            @NotBlank @Email String email,
            @Size(max = 30) String telefono,
            @NotBlank @Size(min = 6, max = 100) String password,
            @NotEmpty List<String> roles
    ) {}

    public record RoleAssignmentRequest(
            @NotEmpty List<String> roles
    ) {}

    public record UserStatusRequest(
            @NotNull Boolean activo
    ) {}

    public record UserResponse(
            UUID id,
            String nombreCompleto,
            String email,
            String telefono,
            boolean activo,
            List<String> roles,
            Instant creadoEn
    ) {}

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserResponse user
    ) {}

    public record RoleResponse(
            UUID id,
            String nombre,
            String descripcion
    ) {}

    public record AutorizacionCreateRequest(
            @NotBlank String tipo,
            @NotNull UUID referenciaExternaId,
            @NotBlank String motivo
    ) {}

    public record AutorizacionResolveRequest(
            String observacionResolucion
    ) {}

    public record AutorizacionResponse(
            UUID id,
            String tipo,
            UUID referenciaExternaId,
            UUID solicitadoPorId,
            UUID aprobadoPorId,
            String motivo,
            String estado,
            String observacionResolucion,
            Instant creadoEn,
            Instant resueltoEn
    ) {}

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}
}
