package com.arenacore.billing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class BillingDtos {

    private BillingDtos() {}

    public record TarifaCanchaResponse(
            UUID id,
            String tipoCancha,
            String franja,
            BigDecimal valorHoraCop,
            Instant vigenteDesde,
            boolean activa
    ) {}

    public record TarifaImplementoResponse(
            UUID id,
            String codigoTarifa,
            String nombreReferencia,
            BigDecimal valorAlquilerCop,
            BigDecimal valorReposicionCop,
            Instant vigenteDesde,
            boolean activa
    ) {}

    public record TarifasVigentesResponse(
            List<TarifaCanchaResponse> canchas,
            List<TarifaImplementoResponse> implementos
    ) {}

    public record ActualizarTarifaCanchaItem(
            @NotBlank String tipoCancha,
            @NotBlank String franja,
            @NotNull @DecimalMin("0.00") BigDecimal valorHoraCop
    ) {}

    public record ActualizarTarifaImplementoItem(
            @NotBlank String codigoTarifa,
            @NotBlank String nombreReferencia,
            @NotNull @DecimalMin("0.00") BigDecimal valorAlquilerCop,
            @NotNull @DecimalMin("0.00") BigDecimal valorReposicionCop
    ) {}

    public record ActualizarTarifasRequest(
            @Valid List<ActualizarTarifaCanchaItem> canchas,
            @Valid List<ActualizarTarifaImplementoItem> implementos
    ) {}

    public record CargoImplementoRequest(
            @NotBlank String codigoTarifa,
            @NotBlank String nombreImplemento,
            @Min(0) int cantidadPrestada,
            @Min(0) int cantidadDanada,
            @Min(0) int cantidadPerdida
    ) {}

    public record CrearLiquidacionRequest(
            @NotNull UUID sesionJuegoId,
            @NotBlank String canchaNombre,
            @NotBlank String tipoCancha,
            @Min(0) int horasDiurnas,
            @Min(0) int horasNocturnas,
            @Min(0) int horasAdicionales,
            String franjaHoraAdicional,
            BigDecimal abonoPrevioCop,
            BigDecimal penalidadCancelacionCop,
            @Valid List<CargoImplementoRequest> cargosImplementos
    ) {}

    public record DetalleLiquidacionResponse(
            UUID id,
            String concepto,
            String descripcion,
            int cantidad,
            BigDecimal valorUnitarioSnapshotCop,
            BigDecimal subtotalCop
    ) {}

    public record LiquidacionResponse(
            UUID id,
            UUID sesionJuegoId,
            String canchaNombre,
            String tipoCancha,
            BigDecimal abonoPrevioCop,
            BigDecimal subtotalCanchaCop,
            BigDecimal subtotalImplementosCop,
            BigDecimal subtotalPenalidadesCop,
            BigDecimal totalCop,
            BigDecimal saldoPendienteCop,
            String estado,
            Instant creadoEn,
            List<DetalleLiquidacionResponse> detalles
    ) {}

    public record AbrirSesionCajaRequest(
            UUID cajaId,
            @NotNull @DecimalMin("0.00") BigDecimal saldoInicialCop
    ) {}

    public record CerrarSesionCajaRequest(
            @NotNull @DecimalMin("0.00") BigDecimal saldoCierreDeclaradoCop
    ) {}

    public record MovimientoCajaResponse(
            UUID id,
            UUID pagoId,
            String tipo,
            String metodoPago,
            BigDecimal montoCop,
            String descripcion,
            Instant creadoEn
    ) {}

    public record SesionCajaResponse(
            UUID id,
            UUID cajaId,
            UUID operadorId,
            BigDecimal saldoInicialCop,
            BigDecimal totalEfectivoCop,
            BigDecimal totalTransferenciaCop,
            BigDecimal totalDatafonoCop,
            BigDecimal saldoCierreDeclaradoCop,
            BigDecimal diferenciaArqueoCop,
            String estado,
            Instant abiertaEn,
            Instant cerradaEn,
            List<MovimientoCajaResponse> movimientos
    ) {}

    public record ResolverPagoRequest(
            UUID sesionCajaId,
            @NotBlank String metodoPago,
            String referenciaTransaccion,
            @NotNull @DecimalMin("0.01") BigDecimal montoCop
    ) {}

    public record PagoResueltoResponse(
            UUID pagoId,
            UUID liquidacionId,
            String estadoLiquidacion,
            String estadoPago,
            String metodoPago,
            BigDecimal montoPagadoCop,
            UUID comprobanteId,
            String numeroComprobante,
            Instant emitidoEn
    ) {}

    public record ComprobanteResponse(
            UUID id,
            String numeroComprobante,
            UUID pagoId,
            UUID liquidacionId,
            BigDecimal totalPagadoCop,
            String detalleJson,
            Instant emitidoEn
    ) {}

    public record CajaResponse(
            UUID id,
            String codigo,
            String nombre,
            String tipo,
            boolean activa
    ) {}

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}
}
