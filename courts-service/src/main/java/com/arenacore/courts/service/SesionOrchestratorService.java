package com.arenacore.courts.service;

import com.arenacore.courts.client.BillingInternalClient;
import com.arenacore.courts.client.EquipmentInternalClient;
import com.arenacore.courts.client.IdentityInternalClient;
import com.arenacore.courts.domain.Reserva;
import com.arenacore.courts.dto.CourtsDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Orquestador de llamadas HTTP síncronas entre microservicios.
 * HT-05 / ADR-006: NO usa @Transactional para evitar mantener conexiones JDBC abiertas durante llamadas HTTP.
 */
@Service
public class SesionOrchestratorService {

    private final CourtsCoreService coreService;
    private final EquipmentInternalClient equipmentClient;
    private final BillingInternalClient billingClient;
    private final IdentityInternalClient identityClient;
    private final IdempotencyService idempotencyService;

    public SesionOrchestratorService(
            CourtsCoreService coreService,
            EquipmentInternalClient equipmentClient,
            BillingInternalClient billingClient,
            IdentityInternalClient identityClient,
            IdempotencyService idempotencyService) {
        this.coreService = coreService;
        this.equipmentClient = equipmentClient;
        this.billingClient = billingClient;
        this.identityClient = identityClient;
        this.idempotencyService = idempotencyService;
    }

    public ReservaResponse cancelarReserva(UUID reservaId, CancelarReservaRequest request) {
        Reserva reserva = coreService.obtenerReservaEntity(reservaId);
        boolean exonerada = false;
        if (request.autorizacionExcepcionalId() != null) {
            exonerada = identityClient.esAutorizacionAprobada(request.autorizacionExcepcionalId(), reserva.getId());
            if (!exonerada) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La autorización excepcional no está APROBADA para esta reserva");
            }
        }
        return coreService.cancelarReservaLocal(reservaId, request, exonerada);
    }

    public LiquidacionInternalResponse solicitarLiquidacion(UUID sesionId, String rawIdempotencyKey, UUID actorId) {
        String key = (rawIdempotencyKey != null && !rawIdempotencyKey.isBlank())
                ? idempotencyService.requireKey(rawIdempotencyKey)
                : "liq-" + sesionId;

        DatosLiquidacionSesion datos = coreService.obtenerDatosParaLiquidacion(sesionId);
        if (datos.sesion().liquidacionId() != null) {
            return billingClient.obtenerEstadoLiquidacion(datos.sesion().liquidacionId());
        }

        // 1. Consultar cargos y préstamos pendientes en Equipment Service (fuera de transacción JDBC)
        CargosSesionInternalResponse cargosEq = equipmentClient.obtenerCargosSesion(sesionId);
        if (cargosEq.tienePrestamosActivos()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Existen préstamos de implementos ACTIVOS sin devolver para esta sesión. Registre la devolución en Equipment Service primero.");
        }

        // 2. Enviar consolidación a Billing Service con Idempotency-Key (fuera de transacción JDBC)
        CrearLiquidacionInternalRequest reqBilling = new CrearLiquidacionInternalRequest(
                sesionId,
                datos.sesion().canchaNombre(),
                datos.sesion().tipoCancha(),
                datos.horasDiurnas(),
                datos.horasNocturnas(),
                datos.horasAdicionales(),
                datos.franjaHoraAdicional(),
                datos.abonoPrevioCop(),
                BigDecimal.ZERO,
                cargosEq.cargos()
        );
        LiquidacionInternalResponse liq = billingClient.crearLiquidacion(reqBilling, key);

        // 3. Guardar referencia de liquidación en transacción local corta
        coreService.registrarLiquidacionEnSesion(sesionId, liq.id(), actorId);
        return liq;
    }

    public SesionJuegoResponse cerrarSesion(UUID sesionId, UUID actorId) {
        SesionJuegoResponse sesion = coreService.obtenerSesion(sesionId);
        if ("CERRADA".equals(sesion.estado())) {
            return sesion;
        }
        if (sesion.liquidacionId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Debe solicitar la liquidación antes de cerrar la sesión de juego");
        }

        // Verificar estado terminal PAGADA en Billing Service fuera de transacción JDBC
        LiquidacionInternalResponse liq = billingClient.obtenerEstadoLiquidacion(sesion.liquidacionId());
        if (!"PAGADA".equals(liq.estado()) && !"GRATUITA".equals(liq.estado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede liberar la cancha: la liquidación " + liq.id()
                            + " aún está en estado " + liq.estado()
                            + " (saldo pendiente: $" + liq.saldoPendienteCop() + ")");
        }

        return coreService.cerrarSesionLocal(sesionId, actorId);
    }
}
