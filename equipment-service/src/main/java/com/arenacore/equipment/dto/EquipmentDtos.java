package com.arenacore.equipment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class EquipmentDtos {

    private EquipmentDtos() {}

    public record ImplementoCreateRequest(
            @NotBlank String categoriaCodigo,
            @NotBlank String codigo,
            @NotBlank String nombre,
            @Min(0) int stockInicial,
            @NotBlank String codigoTarifa
    ) {}

    public record ImplementoResponse(
            UUID id,
            UUID categoriaId,
            String categoriaCodigo,
            String codigo,
            String nombre,
            int stockTotal,
            int stockDisponible,
            int stockMantenimiento,
            String codigoTarifa,
            boolean activo
    ) {}

    public record AjusteStockRequest(
            @NotBlank String tipoMovimiento,
            @Min(1) int cantidad,
            @NotBlank String observacion
    ) {}

    public record MovimientoInventarioResponse(
            UUID id,
            UUID implementoId,
            UUID prestamoId,
            String tipoMovimiento,
            int cantidad,
            int stockAnterior,
            int stockNuevo,
            UUID usuarioId,
            String observacion,
            Instant creadoEn
    ) {}

    public record ItemPrestamoRequest(
            @NotNull UUID implementoId,
            @Min(1) int cantidad
    ) {}

    public record RegistrarPrestamoRequest(
            @NotNull UUID sesionJuegoId,
            String observaciones,
            @NotEmpty @Valid List<ItemPrestamoRequest> items
    ) {}

    public record DetallePrestamoResponse(
            UUID id,
            UUID implementoId,
            String implementoNombre,
            String codigoTarifa,
            int cantidadEntregada,
            int cantidadDevuelta,
            int cantidadDanada,
            int cantidadPerdida,
            String estadoRetorno,
            String observacionNovedad
    ) {}

    public record PrestamoResponse(
            UUID id,
            UUID sesionJuegoId,
            UUID operadorId,
            String estado,
            String observaciones,
            Instant fechaPrestamo,
            Instant fechaDevolucion,
            List<DetallePrestamoResponse> detalles
    ) {}

    public record ItemDevolucionRequest(
            @NotNull UUID detallePrestamoId,
            @Min(0) int cantidadDevueltaBuena,
            @Min(0) int cantidadDanada,
            @Min(0) int cantidadPerdida,
            String observacionNovedad
    ) {}

    public record RegistrarDevolucionRequest(
            @NotEmpty @Valid List<ItemDevolucionRequest> items
    ) {}

    public record CargoImplementoResumen(
            String codigoTarifa,
            String nombreImplemento,
            int cantidadPrestada,
            int cantidadDanada,
            int cantidadPerdida
    ) {}

    public record CargosSesionResponse(
            UUID sesionJuegoId,
            boolean tienePrestamosActivos,
            List<CargoImplementoResumen> cargos
    ) {}

    public record SesionJuegoEstadoResponse(
            UUID id,
            String estado,
            String canchaNombre
    ) {}

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}
}
