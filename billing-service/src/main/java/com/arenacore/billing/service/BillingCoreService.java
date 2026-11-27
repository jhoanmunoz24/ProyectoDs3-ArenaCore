package com.arenacore.billing.service;

import com.arenacore.billing.domain.*;
import com.arenacore.billing.dto.BillingDtos.*;
import com.arenacore.billing.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BillingCoreService {

    private static final String DEFAULT_CAJA_CODIGO = "CAJA-01";

    private final TarifaCanchaRepository tarifaCanchaRepo;
    private final TarifaImplementoRepository tarifaImplementoRepo;
    private final LiquidacionRepository liquidacionRepo;
    private final CajaRepository cajaRepo;
    private final SesionCajaRepository sesionCajaRepo;
    private final PagoRepository pagoRepo;
    private final MovimientoCajaRepository movimientoCajaRepo;
    private final ComprobanteRepository comprobanteRepo;
    private final IdempotencyService idempotencyService;

    public BillingCoreService(
            TarifaCanchaRepository tarifaCanchaRepo,
            TarifaImplementoRepository tarifaImplementoRepo,
            LiquidacionRepository liquidacionRepo,
            CajaRepository cajaRepo,
            SesionCajaRepository sesionCajaRepo,
            PagoRepository pagoRepo,
            MovimientoCajaRepository movimientoCajaRepo,
            ComprobanteRepository comprobanteRepo,
            IdempotencyService idempotencyService) {
        this.tarifaCanchaRepo = tarifaCanchaRepo;
        this.tarifaImplementoRepo = tarifaImplementoRepo;
        this.liquidacionRepo = liquidacionRepo;
        this.cajaRepo = cajaRepo;
        this.sesionCajaRepo = sesionCajaRepo;
        this.pagoRepo = pagoRepo;
        this.movimientoCajaRepo = movimientoCajaRepo;
        this.comprobanteRepo = comprobanteRepo;
        this.idempotencyService = idempotencyService;
    }

    @Transactional(readOnly = true)
    public TarifasVigentesResponse obtenerTarifasVigentes() {
        List<TarifaCanchaResponse> canchas = tarifaCanchaRepo.findByActivaTrueOrderByTipoCanchaAscFranjaAsc().stream()
                .map(t -> new TarifaCanchaResponse(
                        t.getId(), t.getTipoCancha(), t.getFranja(), t.getValorHoraCop(), t.getVigenteDesde(), t.isActiva()
                )).toList();

        List<TarifaImplementoResponse> implementos = tarifaImplementoRepo.findByActivaTrueOrderByCodigoTarifaAsc().stream()
                .map(t -> new TarifaImplementoResponse(
                        t.getId(), t.getCodigoTarifa(), t.getNombreReferencia(),
                        t.getValorAlquilerCop(), t.getValorReposicionCop(), t.getVigenteDesde(), t.isActiva()
                )).toList();

        return new TarifasVigentesResponse(canchas, implementos);
    }

    @Transactional
    public TarifasVigentesResponse actualizarTarifasVigentes(ActualizarTarifasRequest request, UUID adminId) {
        Instant now = Instant.now();
        if (request.canchas() != null) {
            for (ActualizarTarifaCanchaItem item : request.canchas()) {
                String tipo = item.tipoCancha().trim().toUpperCase();
                String franja = item.franja().trim().toUpperCase();
                tarifaCanchaRepo.findByTipoCanchaAndFranjaAndActivaTrue(tipo, franja)
                        .ifPresent(prev -> {
                            prev.setActiva(false);
                            prev.setVigenteHasta(now);
                            tarifaCanchaRepo.save(prev);
                        });
                TarifaCancha nueva = new TarifaCancha();
                nueva.setId(UUID.randomUUID());
                nueva.setTipoCancha(tipo);
                nueva.setFranja(franja);
                nueva.setValorHoraCop(item.valorHoraCop());
                nueva.setVigenteDesde(now);
                nueva.setActiva(true);
                nueva.setCreadoPorId(adminId);
                tarifaCanchaRepo.save(nueva);
            }
        }
        if (request.implementos() != null) {
            for (ActualizarTarifaImplementoItem item : request.implementos()) {
                String codigo = item.codigoTarifa().trim().toUpperCase();
                tarifaImplementoRepo.findByCodigoTarifaAndActivaTrue(codigo)
                        .ifPresent(prev -> {
                            prev.setActiva(false);
                            prev.setVigenteHasta(now);
                            tarifaImplementoRepo.save(prev);
                        });
                TarifaImplemento nueva = new TarifaImplemento();
                nueva.setId(UUID.randomUUID());
                nueva.setCodigoTarifa(codigo);
                nueva.setNombreReferencia(item.nombreReferencia().trim());
                nueva.setValorAlquilerCop(item.valorAlquilerCop());
                nueva.setValorReposicionCop(item.valorReposicionCop());
                nueva.setVigenteDesde(now);
                nueva.setActiva(true);
                nueva.setCreadoPorId(adminId);
                tarifaImplementoRepo.save(nueva);
            }
        }
        return obtenerTarifasVigentes();
    }

    @Transactional
    public LiquidacionResponse crearLiquidacionConsolidada(CrearLiquidacionRequest request) {
        Liquidacion existente = liquidacionRepo.findBySesionJuegoId(request.sesionJuegoId()).orElse(null);
        if (existente != null) {
            return toLiquidacionResponse(existente);
        }

        String tipoCancha = request.tipoCancha().trim().toUpperCase();
        Liquidacion liq = new Liquidacion();
        liq.setId(UUID.randomUUID());
        liq.setSesionJuegoId(request.sesionJuegoId());
        liq.setCanchaNombre(request.canchaNombre().trim());
        liq.setTipoCancha(tipoCancha);

        BigDecimal subtotalCancha = BigDecimal.ZERO;
        if (request.horasDiurnas() > 0) {
            TarifaCancha tDiurna = getActiveCourtTariff(tipoCancha, "DIURNA");
            BigDecimal sub = tDiurna.getValorHoraCop().multiply(BigDecimal.valueOf(request.horasDiurnas()));
            subtotalCancha = subtotalCancha.add(sub);
            liq.getDetalles().add(new DetalleLiquidacion(
                    liq, "HORA_DIURNA", "Uso de cancha " + tipoCancha + " en franja diurna",
                    request.horasDiurnas(), tDiurna.getValorHoraCop(), sub
            ));
        }

        if (request.horasNocturnas() > 0) {
            TarifaCancha tNocturna = getActiveCourtTariff(tipoCancha, "NOCTURNA");
            BigDecimal sub = tNocturna.getValorHoraCop().multiply(BigDecimal.valueOf(request.horasNocturnas()));
            subtotalCancha = subtotalCancha.add(sub);
            liq.getDetalles().add(new DetalleLiquidacion(
                    liq, "HORA_NOCTURNA", "Uso de cancha " + tipoCancha + " en franja nocturna (iluminación)",
                    request.horasNocturnas(), tNocturna.getValorHoraCop(), sub
            ));
        }

        if (request.horasAdicionales() > 0) {
            String franjaExtra = (request.franjaHoraAdicional() != null && !request.franjaHoraAdicional().isBlank())
                    ? request.franjaHoraAdicional().trim().toUpperCase()
                    : "NOCTURNA";
            TarifaCancha tExtra = getActiveCourtTariff(tipoCancha, franjaExtra);
            BigDecimal sub = tExtra.getValorHoraCop().multiply(BigDecimal.valueOf(request.horasAdicionales()));
            subtotalCancha = subtotalCancha.add(sub);
            liq.getDetalles().add(new DetalleLiquidacion(
                    liq, "HORA_ADICIONAL", "Hora adicional superado tiempo de gracia (" + franjaExtra + ")",
                    request.horasAdicionales(), tExtra.getValorHoraCop(), sub
            ));
        }

        BigDecimal subtotalImplementos = BigDecimal.ZERO;
        BigDecimal subtotalPenalidades = BigDecimal.ZERO;

        if (request.cargosImplementos() != null) {
            for (CargoImplementoRequest cargo : request.cargosImplementos()) {
                TarifaImplemento tarifaImp = tarifaImplementoRepo
                        .findByCodigoTarifaAndActivaTrue(cargo.codigoTarifa().trim().toUpperCase())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Tarifa de implemento no encontrada: " + cargo.codigoTarifa()));

                if (cargo.cantidadPrestada() > 0) {
                    BigDecimal subAlq = tarifaImp.getValorAlquilerCop()
                            .multiply(BigDecimal.valueOf(cargo.cantidadPrestada()));
                    subtotalImplementos = subtotalImplementos.add(subAlq);
                    liq.getDetalles().add(new DetalleLiquidacion(
                            liq, "ALQUILER_IMPLEMENTO", "Alquiler: " + cargo.nombreImplemento(),
                            cargo.cantidadPrestada(), tarifaImp.getValorAlquilerCop(), subAlq
                    ));
                }

                if (cargo.cantidadDanada() > 0) {
                    BigDecimal subDano = tarifaImp.getValorReposicionCop()
                            .multiply(BigDecimal.valueOf(cargo.cantidadDanada()));
                    subtotalPenalidades = subtotalPenalidades.add(subDano);
                    liq.getDetalles().add(new DetalleLiquidacion(
                            liq, "PENALIDAD_DANO", "Reposición por daño: " + cargo.nombreImplemento(),
                            cargo.cantidadDanada(), tarifaImp.getValorReposicionCop(), subDano
                    ));
                }

                if (cargo.cantidadPerdida() > 0) {
                    BigDecimal subPerd = tarifaImp.getValorReposicionCop()
                            .multiply(BigDecimal.valueOf(cargo.cantidadPerdida()));
                    subtotalPenalidades = subtotalPenalidades.add(subPerd);
                    liq.getDetalles().add(new DetalleLiquidacion(
                            liq, "PENALIDAD_PERDIDA", "Reposición por pérdida: " + cargo.nombreImplemento(),
                            cargo.cantidadPerdida(), tarifaImp.getValorReposicionCop(), subPerd
                    ));
                }
            }
        }

        if (request.penalidadCancelacionCop() != null && request.penalidadCancelacionCop().compareTo(BigDecimal.ZERO) > 0) {
            subtotalPenalidades = subtotalPenalidades.add(request.penalidadCancelacionCop());
            liq.getDetalles().add(new DetalleLiquidacion(
                    liq, "PENALIDAD_CANCELACION", "Cargo administrativo por cancelación tardía",
                    1, request.penalidadCancelacionCop(), request.penalidadCancelacionCop()
            ));
        }

        BigDecimal abonoPrevio = request.abonoPrevioCop() != null ? request.abonoPrevioCop() : BigDecimal.ZERO;
        BigDecimal totalBruto = subtotalCancha.add(subtotalImplementos).add(subtotalPenalidades);
        BigDecimal saldoPendiente = totalBruto.subtract(abonoPrevio).max(BigDecimal.ZERO);

        if (abonoPrevio.compareTo(BigDecimal.ZERO) > 0) {
            liq.getDetalles().add(new DetalleLiquidacion(
                    liq, "DESCUENTO_ABONO", "Abono previo aplicado a la reserva",
                    1, abonoPrevio.negate(), abonoPrevio.negate()
            ));
        }

        liq.setAbonoPrevioCop(abonoPrevio);
        liq.setSubtotalCanchaCop(subtotalCancha);
        liq.setSubtotalImplementosCop(subtotalImplementos);
        liq.setSubtotalPenalidadesCop(subtotalPenalidades);
        liq.setTotalCop(totalBruto);
        liq.setSaldoPendienteCop(saldoPendiente);
        liq.setEstado(saldoPendiente.compareTo(BigDecimal.ZERO) == 0 ? "PAGADA" : "PENDIENTE");

        liquidacionRepo.save(liq);
        return toLiquidacionResponse(liq);
    }

    @Transactional(readOnly = true)
    public LiquidacionResponse obtenerLiquidacion(UUID id) {
        Liquidacion liq = liquidacionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liquidación no encontrada"));
        return toLiquidacionResponse(liq);
    }

    @Transactional(readOnly = true)
    public LiquidacionResponse obtenerLiquidacionPorSesion(UUID sesionJuegoId) {
        Liquidacion liq = liquidacionRepo.findBySesionJuegoId(sesionJuegoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe liquidación para la sesión"));
        return toLiquidacionResponse(liq);
    }

    @Transactional(readOnly = true)
    public List<CajaResponse> listarCajas() {
        return cajaRepo.findAll().stream()
                .map(c -> new CajaResponse(c.getId(), c.getCodigo(), c.getNombre(), c.getTipo(), c.isActiva()))
                .toList();
    }

    @Transactional
    public SesionCajaResponse abrirSesionCaja(AbrirSesionCajaRequest request, UUID operadorId) {
        Caja caja = request.cajaId() != null
                ? cajaRepo.findById(request.cajaId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caja no encontrada"))
                : cajaRepo.findByCodigo(DEFAULT_CAJA_CODIGO)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caja principal no configurada"));

        if (sesionCajaRepo.findByCajaIdAndEstado(caja.getId(), "ABIERTA").isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una sesión de caja ABIERTA para " + caja.getNombre());
        }

        SesionCaja sesion = new SesionCaja();
        sesion.setId(UUID.randomUUID());
        sesion.setCajaId(caja.getId());
        sesion.setOperadorId(operadorId);
        sesion.setSaldoInicialCop(request.saldoInicialCop());
        sesion.setEstado("ABIERTA");
        sesionCajaRepo.save(sesion);

        return toSesionCajaResponse(sesion, List.of());
    }

    @Transactional
    public SesionCajaResponse cerrarSesionCaja(UUID sesionCajaId, CerrarSesionCajaRequest request) {
        SesionCaja sesion = sesionCajaRepo.findById(sesionCajaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de caja no encontrada"));
        if (!"ABIERTA".equals(sesion.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión de caja ya se encuentra cerrada");
        }

        BigDecimal efectivoEsperado = sesion.getSaldoInicialCop().add(sesion.getTotalEfectivoCop());
        BigDecimal diferencia = request.saldoCierreDeclaradoCop().subtract(efectivoEsperado);

        sesion.setSaldoCierreDeclaradoCop(request.saldoCierreDeclaradoCop());
        sesion.setDiferenciaArqueoCop(diferencia);
        sesion.setEstado("CERRADA");
        sesion.setCerradaEn(Instant.now());
        sesionCajaRepo.save(sesion);

        return consultarArqueoCaja(sesionCajaId);
    }

    @Transactional(readOnly = true)
    public SesionCajaResponse consultarArqueoCaja(UUID sesionCajaId) {
        SesionCaja sesion = sesionCajaRepo.findById(sesionCajaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de caja no encontrada"));
        List<MovimientoCajaResponse> movs = movimientoCajaRepo.findBySesionCajaIdOrderByCreadoEnAsc(sesionCajaId)
                .stream()
                .map(m -> new MovimientoCajaResponse(
                        m.getId(), m.getPagoId(), m.getTipo(), m.getMetodoPago(),
                        m.getMontoCop(), m.getDescripcion(), m.getCreadoEn()
                )).toList();
        return toSesionCajaResponse(sesion, movs);
    }

    @Transactional
    public PagoResueltoResponse resolverPago(UUID targetId, ResolverPagoRequest request, UUID operadorId) {
        String metodo = request.metodoPago().trim().toUpperCase();
        if (!List.of("EFECTIVO", "TRANSFERENCIA", "DATAFONO").contains(metodo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Método de pago inválido (EFECTIVO, TRANSFERENCIA, DATAFONO)");
        }

        Pago pagoExistente = pagoRepo.findById(targetId).orElse(null);
        UUID liquidacionId = pagoExistente != null ? pagoExistente.getLiquidacionId() : targetId;

        Liquidacion liquidacion = liquidacionRepo.findById(liquidacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liquidación o pago no encontrado"));

        if (!"PENDIENTE".equals(liquidacion.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La liquidación no está PENDIENTE (estado actual: " + liquidacion.getEstado() + ")");
        }

        if (request.montoCop().compareTo(liquidacion.getSaldoPendienteCop()) != 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Se requiere pago completo sin saldo restante. Saldo pendiente: $" + liquidacion.getSaldoPendienteCop());
        }

        SesionCaja sesionCaja = resolverSesionCajaAbierta(request.sesionCajaId(), operadorId);

        Pago pago = pagoExistente != null ? pagoExistente : new Pago();
        if (pago.getId() == null) {
            pago.setId(UUID.randomUUID());
        }
        pago.setLiquidacionId(liquidacion.getId());
        pago.setSesionCajaId(sesionCaja.getId());
        pago.setMetodoPago(metodo);
        pago.setReferenciaTransaccion(request.referenciaTransaccion());
        pago.setMontoCop(request.montoCop());
        pago.setEstado("APROBADO");
        pago.setOperadorId(operadorId);
        pagoRepo.save(pago);

        switch (metodo) {
            case "EFECTIVO" -> sesionCaja.setTotalEfectivoCop(sesionCaja.getTotalEfectivoCop().add(request.montoCop()));
            case "TRANSFERENCIA" -> sesionCaja.setTotalTransferenciaCop(sesionCaja.getTotalTransferenciaCop().add(request.montoCop()));
            case "DATAFONO" -> sesionCaja.setTotalDatafonoCop(sesionCaja.getTotalDatafonoCop().add(request.montoCop()));
        }
        sesionCajaRepo.save(sesionCaja);

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setId(UUID.randomUUID());
        movimiento.setSesionCajaId(sesionCaja.getId());
        movimiento.setPagoId(pago.getId());
        movimiento.setTipo("INGRESO_PAGO");
        movimiento.setMetodoPago(metodo);
        movimiento.setMontoCop(request.montoCop());
        movimiento.setDescripcion("Recaudo liquidación sesión " + liquidacion.getSesionJuegoId());
        movimientoCajaRepo.save(movimiento);

        liquidacion.setSaldoPendienteCop(BigDecimal.ZERO);
        liquidacion.setEstado("PAGADA");
        liquidacion.setActualizadoEn(Instant.now());
        liquidacionRepo.save(liquidacion);

        Comprobante comprobante = new Comprobante();
        comprobante.setId(UUID.randomUUID());
        comprobante.setNumeroComprobante("CMP-" + System.currentTimeMillis());
        comprobante.setPagoId(pago.getId());
        comprobante.setLiquidacionId(liquidacion.getId());
        comprobante.setTotalPagadoCop(request.montoCop());
        comprobante.setDetalleJson(idempotencyService.toJson(toLiquidacionResponse(liquidacion)));
        comprobanteRepo.save(comprobante);

        return new PagoResueltoResponse(
                pago.getId(),
                liquidacion.getId(),
                liquidacion.getEstado(),
                pago.getEstado(),
                pago.getMetodoPago(),
                pago.getMontoCop(),
                comprobante.getId(),
                comprobante.getNumeroComprobante(),
                comprobante.getEmitidoEn()
        );
    }

    @Transactional(readOnly = true)
    public ComprobanteResponse obtenerComprobante(UUID id) {
        Comprobante c = comprobanteRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprobante no encontrado"));
        return toComprobanteResponse(c);
    }

    @Transactional(readOnly = true)
    public ComprobanteResponse obtenerComprobantePorLiquidacion(UUID liquidacionId) {
        Comprobante c = comprobanteRepo.findByLiquidacionId(liquidacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprobante no encontrado para la liquidación"));
        return toComprobanteResponse(c);
    }

    private SesionCaja resolverSesionCajaAbierta(UUID sesionCajaId, UUID operadorId) {
        if (sesionCajaId != null) {
            SesionCaja sc = sesionCajaRepo.findById(sesionCajaId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de caja no encontrada"));
            if (!"ABIERTA".equals(sc.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión de caja indicada está CERRADA");
            }
            return sc;
        }
        return sesionCajaRepo.findFirstByOperadorIdAndEstadoOrderByAbiertaEnDesc(operadorId, "ABIERTA")
                .or(() -> sesionCajaRepo.findFirstByEstadoOrderByAbiertaEnDesc("ABIERTA"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No hay ninguna sesión de caja ABIERTA. Debe abrir caja antes de registrar pagos."));
    }

    private TarifaCancha getActiveCourtTariff(String tipoCancha, String franja) {
        return tarifaCanchaRepo.findByTipoCanchaAndFranjaAndActivaTrue(tipoCancha, franja)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tarifa no configurada para " + tipoCancha + " en franja " + franja));
    }

    private LiquidacionResponse toLiquidacionResponse(Liquidacion l) {
        List<DetalleLiquidacionResponse> dets = l.getDetalles().stream()
                .map(d -> new DetalleLiquidacionResponse(
                        d.getId(), d.getConcepto(), d.getDescripcion(),
                        d.getCantidad(), d.getValorUnitarioSnapshotCop(), d.getSubtotalCop()
                )).toList();
        return new LiquidacionResponse(
                l.getId(), l.getSesionJuegoId(), l.getCanchaNombre(), l.getTipoCancha(),
                l.getAbonoPrevioCop(), l.getSubtotalCanchaCop(), l.getSubtotalImplementosCop(),
                l.getSubtotalPenalidadesCop(), l.getTotalCop(), l.getSaldoPendienteCop(),
                l.getEstado(), l.getCreadoEn(), dets
        );
    }

    private SesionCajaResponse toSesionCajaResponse(SesionCaja s, List<MovimientoCajaResponse> movs) {
        return new SesionCajaResponse(
                s.getId(), s.getCajaId(), s.getOperadorId(), s.getSaldoInicialCop(),
                s.getTotalEfectivoCop(), s.getTotalTransferenciaCop(), s.getTotalDatafonoCop(),
                s.getSaldoCierreDeclaradoCop(), s.getDiferenciaArqueoCop(),
                s.getEstado(), s.getAbiertaEn(), s.getCerradaEn(), movs
        );
    }

    private ComprobanteResponse toComprobanteResponse(Comprobante c) {
        return new ComprobanteResponse(
                c.getId(), c.getNumeroComprobante(), c.getPagoId(), c.getLiquidacionId(),
                c.getTotalPagadoCop(), c.getDetalleJson(), c.getEmitidoEn()
        );
    }
}
