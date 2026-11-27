package com.arenacore.courts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class CourtsDtos {

    private CourtsDtos() {}

    public record SedeCreateRequest(
            @NotBlank String nombre,
            @NotBlank String direccion,
            String telefono
    ) {}

    public record SedeResponse(
            UUID id,
            String nombre,
            String direccion,
            String telefono,
            LocalTime horaApertura,
            LocalTime horaCierre,
            String zonaHoraria,
            boolean activo
    ) {}

    public record CanchaCreateRequest(
            @NotNull UUID sedeId,
            @NotBlank String nombre,
            @NotBlank String tipo,
            @Min(2) int capacidadJugadores,
            String superficie,
            Boolean tieneIluminacion
    ) {}

    public record CanchaEstadoRequest(
            @NotBlank String estadoOperativo
    ) {}

    public record MantenimientoRequest(
            @NotNull LocalDate fecha,
            @NotNull LocalTime horaInicio,
            @NotNull LocalTime horaFin,
            @NotBlank String motivo
    ) {}

    public record CanchaResponse(
            UUID id,
            UUID sedeId,
            String nombre,
            String tipo,
            int capacidadJugadores,
            String superficie,
            boolean tieneIluminacion,
            String estadoOperativo
    ) {}

    public record BloqueDisponibilidadResponse(
            int hora,
            String franja,
            String estado
    ) {}

    public record CanchaDisponibilidadResponse(
            UUID canchaId,
            String canchaNombre,
            String tipoCancha,
            String estadoOperativo,
            List<BloqueDisponibilidadResponse> bloques
    ) {}

    public record DisponibilidadDiaResponse(
            LocalDate fecha,
            List<CanchaDisponibilidadResponse> canchas
    ) {}

    public record CrearReservaRequest(
            @NotNull UUID canchaId,
            @NotBlank String nombreCapitan,
            @NotBlank String telefonoCapitan,
            String nombreEquipo,
            @NotNull LocalDate fecha,
            @NotNull LocalTime horaInicio,
            @Min(1) @Max(6) int duracionHoras,
            @DecimalMin("0.00") BigDecimal abonoCop
    ) {}

    public record CancelarReservaRequest(
            @NotBlank String motivo,
            UUID autorizacionExcepcionalId
    ) {}

    public record ReservaResponse(
            UUID id,
            String codigoReserva,
            UUID canchaId,
            String nombreCapitan,
            String telefonoCapitan,
            String nombreEquipo,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            int duracionHoras,
            String estado,
            BigDecimal abonoCop,
            Integer cargoAdministrativoPorcentaje,
            Instant creadoEn
    ) {}

    public record CheckInRequest(
            UUID reservaId,
            UUID canchaId,
            String nombreCapitan,
            String telefonoCapitan,
            String nombreEquipo,
            LocalDate fecha,
            LocalTime horaInicio,
            Integer duracionHoras
    ) {}

    public record SesionJuegoResponse(
            UUID id,
            UUID reservaId,
            UUID canchaId,
            String canchaNombre,
            String tipoCancha,
            String nombreCapitan,
            String telefonoCapitan,
            String nombreEquipo,
            LocalDate fecha,
            Instant horaEntrada,
            Instant horaSalidaProgramada,
            Instant horaSalidaReal,
            int duracionHorasReservadas,
            UUID liquidacionId,
            String estado,
            long minutosTranscurridos
    ) {}

    public record SesionJuegoInternalResponse(
            UUID id,
            String estado,
            String canchaNombre
    ) {}

    public record CargoImplementoInternal(
            String codigoTarifa,
            String nombreImplemento,
            int cantidadPrestada,
            int cantidadDanada,
            int cantidadPerdida
    ) {}

    public record CargosSesionInternalResponse(
            UUID sesionJuegoId,
            boolean tienePrestamosActivos,
            List<CargoImplementoInternal> cargos
    ) {}

    public record CrearLiquidacionInternalRequest(
            UUID sesionJuegoId,
            String canchaNombre,
            String tipoCancha,
            int horasDiurnas,
            int horasNocturnas,
            int horasAdicionales,
            String franjaHoraAdicional,
            BigDecimal abonoPrevioCop,
            BigDecimal penalidadCancelacionCop,
            List<CargoImplementoInternal> cargosImplementos
    ) {}

    public record LiquidacionInternalResponse(
            UUID id,
            UUID sesionJuegoId,
            BigDecimal totalCop,
            BigDecimal saldoPendienteCop,
            String estado
    ) {}

    public record AutorizacionInternalResponse(
            UUID id,
            String tipo,
            UUID referenciaExternaId,
            String estado
    ) {}

    public record DatosLiquidacionSesion(
            SesionJuegoResponse sesion,
            int horasDiurnas,
            int horasNocturnas,
            int horasAdicionales,
            String franjaHoraAdicional,
            BigDecimal abonoPrevioCop
    ) {}

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}
}
