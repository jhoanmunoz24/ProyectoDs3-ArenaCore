package com.arenacore.equipment.service;

import com.arenacore.equipment.domain.*;
import com.arenacore.equipment.dto.EquipmentDtos.*;
import com.arenacore.equipment.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class EquipmentCoreService {

    private final CategoriaImplementoRepository categoriaRepo;
    private final ImplementoRepository implementoRepo;
    private final PrestamoRepository prestamoRepo;
    private final MovimientoInventarioRepository kardexRepo;
    private final IdempotencyService idempotencyService;

    public EquipmentCoreService(
            CategoriaImplementoRepository categoriaRepo,
            ImplementoRepository implementoRepo,
            PrestamoRepository prestamoRepo,
            MovimientoInventarioRepository kardexRepo,
            IdempotencyService idempotencyService) {
        this.categoriaRepo = categoriaRepo;
        this.implementoRepo = implementoRepo;
        this.prestamoRepo = prestamoRepo;
        this.kardexRepo = kardexRepo;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public ImplementoResponse crearImplemento(ImplementoCreateRequest request, UUID adminId) {
        String codigo = request.codigo().trim().toUpperCase();
        if (implementoRepo.existsByCodigoIgnoreCase(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un implemento con código " + codigo);
        }
        CategoriaImplemento cat = categoriaRepo.findByCodigoIgnoreCase(request.categoriaCodigo().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Categoría inválida: " + request.categoriaCodigo()));

        Implemento imp = new Implemento();
        imp.setId(UUID.randomUUID());
        imp.setCategoriaId(cat.getId());
        imp.setCodigo(codigo);
        imp.setNombre(request.nombre().trim());
        imp.setStockTotal(request.stockInicial());
        imp.setStockDisponible(request.stockInicial());
        imp.setStockMantenimiento(0);
        imp.setCodigoTarifa(request.codigoTarifa().trim().toUpperCase());
        imp.setActivo(true);
        implementoRepo.save(imp);

        if (request.stockInicial() > 0) {
            registrarKardex(imp.getId(), null, "ENTRADA_INICIAL", request.stockInicial(), 0,
                    request.stockInicial(), adminId, "Alta inicial en catálogo");
        }
        return toImplementoResponse(imp, cat.getCodigo());
    }

    @Transactional(readOnly = true)
    public List<ImplementoResponse> listarImplementos(boolean soloDisponibles) {
        Map<UUID, String> catCodes = new HashMap<>();
        categoriaRepo.findAll().forEach(c -> catCodes.put(c.getId(), c.getCodigo()));

        List<Implemento> lista = soloDisponibles
                ? implementoRepo.findByActivoTrueAndStockDisponibleGreaterThanOrderByNombreAsc(0)
                : implementoRepo.findAll();

        return lista.stream()
                .map(i -> toImplementoResponse(i, catCodes.getOrDefault(i.getCategoriaId(), "GENERAL")))
                .toList();
    }

    @Transactional
    public ImplementoResponse ajustarStock(UUID implementoId, AjusteStockRequest request, UUID adminId) {
        Implemento imp = implementoRepo.findByIdForUpdate(implementoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Implemento no encontrado"));

        String tipo = request.tipoMovimiento().trim().toUpperCase();
        int anterior = imp.getStockDisponible();
        switch (tipo) {
            case "ENTRADA", "COMPRA" -> {
                imp.setStockTotal(imp.getStockTotal() + request.cantidad());
                imp.setStockDisponible(imp.getStockDisponible() + request.cantidad());
            }
            case "BAJA_DETERIORO" -> {
                if (imp.getStockDisponible() < request.cantidad()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock disponible insuficiente para dar de baja");
                }
                imp.setStockDisponible(imp.getStockDisponible() - request.cantidad());
                imp.setStockTotal(imp.getStockTotal() - request.cantidad());
            }
            case "REINGRESO_MANTENIMIENTO" -> {
                if (imp.getStockMantenimiento() < request.cantidad()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay suficientes unidades en mantenimiento");
                }
                imp.setStockMantenimiento(imp.getStockMantenimiento() - request.cantidad());
                imp.setStockDisponible(imp.getStockDisponible() + request.cantidad());
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de ajuste no soportado: " + tipo);
        }

        imp.setActualizadoEn(Instant.now());
        implementoRepo.save(imp);
        registrarKardex(imp.getId(), null, tipo, request.cantidad(), anterior, imp.getStockDisponible(), adminId, request.observacion());

        String catCode = categoriaRepo.findById(imp.getCategoriaId()).map(CategoriaImplemento::getCodigo).orElse("GENERAL");
        return toImplementoResponse(imp, catCode);
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> consultarKardex(UUID implementoId) {
        return kardexRepo.findByImplementoIdOrderByCreadoEnDesc(implementoId).stream()
                .map(m -> new MovimientoInventarioResponse(
                        m.getId(), m.getImplementoId(), m.getPrestamoId(), m.getTipoMovimiento(),
                        m.getCantidad(), m.getStockAnterior(), m.getStockNuevo(),
                        m.getUsuarioId(), m.getObservacion(), m.getCreadoEn()
                )).toList();
    }

    @Transactional
    public ResponseEntity<String> registrarPrestamoLocal(
            String idempotencyKey,
            String endpoint,
            RegistrarPrestamoRequest request,
            UUID operadorId) {
        Optional<ResponseEntity<String>> replay = idempotencyService.findReplay(idempotencyKey, endpoint, request);
        if (replay.isPresent()) {
            return replay.get();
        }

        Prestamo prestamo = new Prestamo();
        prestamo.setId(UUID.randomUUID());
        prestamo.setSesionJuegoId(request.sesionJuegoId());
        prestamo.setOperadorId(operadorId);
        prestamo.setEstado("ACTIVO");
        prestamo.setObservaciones(request.observaciones());

        Map<UUID, Implemento> implementosMap = new HashMap<>();
        for (ItemPrestamoRequest item : request.items()) {
            Implemento imp = implementoRepo.findByIdForUpdate(item.implementoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Implemento no encontrado: " + item.implementoId()));
            if (!imp.isActivo() || imp.getStockDisponible() < item.cantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para " + imp.getNombre() + " (disponible: " + imp.getStockDisponible() + ")");
            }
            int anterior = imp.getStockDisponible();
            imp.setStockDisponible(anterior - item.cantidad());
            imp.setActualizadoEn(Instant.now());
            implementoRepo.save(imp);
            implementosMap.put(imp.getId(), imp);

            DetallePrestamo det = new DetallePrestamo();
            det.setId(UUID.randomUUID());
            det.setPrestamo(prestamo);
            det.setImplementoId(imp.getId());
            det.setCantidadEntregada(item.cantidad());
            det.setEstadoRetorno("PENDIENTE");
            prestamo.getDetalles().add(det);
        }

        prestamoRepo.save(prestamo);
        for (DetallePrestamo det : prestamo.getDetalles()) {
            Implemento imp = implementosMap.get(det.getImplementoId());
            int stockNuevo = imp.getStockDisponible();
            registrarKardex(imp.getId(), prestamo.getId(), "SALIDA_PRESTAMO", det.getCantidadEntregada(),
                    stockNuevo + det.getCantidadEntregada(), stockNuevo, operadorId,
                    "Préstamo vinculado a sesión " + prestamo.getSesionJuegoId());
        }

        PrestamoResponse response = toPrestamoResponse(prestamo, implementosMap);
        return idempotencyService.saveAndBuildResponse(idempotencyKey, endpoint, request, HttpStatus.CREATED, response);
    }

    @Transactional
    public PrestamoResponse registrarDevolucion(UUID prestamoId, RegistrarDevolucionRequest request, UUID operadorId) {
        Prestamo prestamo = prestamoRepo.findById(prestamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Préstamo no encontrado"));

        if (!"ACTIVO".equals(prestamo.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El préstamo ya fue devuelto");
        }

        Map<UUID, ItemDevolucionRequest> reqByDetalle = new HashMap<>();
        request.items().forEach(i -> reqByDetalle.put(i.detallePrestamoId(), i));

        boolean conNovedad = false;
        Map<UUID, Implemento> implementosMap = new HashMap<>();

        for (DetallePrestamo det : prestamo.getDetalles()) {
            ItemDevolucionRequest itemReq = reqByDetalle.get(det.getId());
            if (itemReq == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Falta el detalle de devolución para el ítem " + det.getId());
            }
            int suma = itemReq.cantidadDevueltaBuena() + itemReq.cantidadDanada() + itemReq.cantidadPerdida();
            if (suma != det.getCantidadEntregada()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La suma de buenas + dañadas + perdidas debe ser igual a la cantidad entregada (" + det.getCantidadEntregada() + ")");
            }

            Implemento imp = implementoRepo.findByIdForUpdate(det.getImplementoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Implemento no encontrado"));
            int stockAnterior = imp.getStockDisponible();

            if (itemReq.cantidadDevueltaBuena() > 0) {
                imp.setStockDisponible(imp.getStockDisponible() + itemReq.cantidadDevueltaBuena());
                registrarKardex(imp.getId(), prestamo.getId(), "RETORNO_BUENO",
                        itemReq.cantidadDevueltaBuena(), stockAnterior, imp.getStockDisponible(),
                        operadorId, "Retorno en buen estado");
                stockAnterior = imp.getStockDisponible();
            }
            if (itemReq.cantidadDanada() > 0) {
                conNovedad = true;
                imp.setStockMantenimiento(imp.getStockMantenimiento() + itemReq.cantidadDanada());
                registrarKardex(imp.getId(), prestamo.getId(), "BAJA_DANO",
                        itemReq.cantidadDanada(), stockAnterior, stockAnterior,
                        operadorId, "Devolución con daño: " + itemReq.observacionNovedad());
            }
            if (itemReq.cantidadPerdida() > 0) {
                conNovedad = true;
                imp.setStockTotal(Math.max(0, imp.getStockTotal() - itemReq.cantidadPerdida()));
                registrarKardex(imp.getId(), prestamo.getId(), "BAJA_PERDIDA",
                        itemReq.cantidadPerdida(), stockAnterior, stockAnterior,
                        operadorId, "Implemento reportado como perdido: " + itemReq.observacionNovedad());
            }

            imp.setActualizadoEn(Instant.now());
            implementoRepo.save(imp);
            implementosMap.put(imp.getId(), imp);

            det.setCantidadDevuelta(itemReq.cantidadDevueltaBuena());
            det.setCantidadDanada(itemReq.cantidadDanada());
            det.setCantidadPerdida(itemReq.cantidadPerdida());
            det.setObservacionNovedad(itemReq.observacionNovedad());
            if (itemReq.cantidadPerdida() > 0) {
                det.setEstadoRetorno("PERDIDO");
            } else if (itemReq.cantidadDanada() > 0) {
                det.setEstadoRetorno("DANADO");
            } else {
                det.setEstadoRetorno("BUENO");
            }
        }

        prestamo.setEstado(conNovedad ? "DEVUELTO_CON_NOVEDAD" : "DEVUELTO");
        prestamo.setFechaDevolucion(Instant.now());
        prestamoRepo.save(prestamo);

        return toPrestamoResponse(prestamo, implementosMap);
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarPrestamosPorSesion(UUID sesionJuegoId) {
        List<Prestamo> prestamos = prestamoRepo.findBySesionJuegoIdOrderByFechaPrestamoAsc(sesionJuegoId);
        Map<UUID, Implemento> implementosMap = loadImplementosMap();
        return prestamos.stream().map(p -> toPrestamoResponse(p, implementosMap)).toList();
    }

    @Transactional(readOnly = true)
    public CargosSesionResponse obtenerCargosPorSesion(UUID sesionJuegoId) {
        List<Prestamo> prestamos = prestamoRepo.findBySesionJuegoIdOrderByFechaPrestamoAsc(sesionJuegoId);
        Map<UUID, Implemento> implementosMap = loadImplementosMap();

        boolean tieneActivos = prestamos.stream().anyMatch(p -> "ACTIVO".equals(p.getEstado()));
        Map<String, CargoImplementoResumen> acumulado = new LinkedHashMap<>();

        for (Prestamo p : prestamos) {
            for (DetallePrestamo d : p.getDetalles()) {
                Implemento imp = implementosMap.get(d.getImplementoId());
                if (imp == null) continue;
                acumulado.merge(
                        imp.getCodigoTarifa(),
                        new CargoImplementoResumen(imp.getCodigoTarifa(), imp.getNombre(),
                                d.getCantidadEntregada(), d.getCantidadDanada(), d.getCantidadPerdida()),
                        (a, b) -> new CargoImplementoResumen(
                                a.codigoTarifa(), a.nombreImplemento(),
                                a.cantidadPrestada() + b.cantidadPrestada(),
                                a.cantidadDanada() + b.cantidadDanada(),
                                a.cantidadPerdida() + b.cantidadPerdida()
                        )
                );
            }
        }

        return new CargosSesionResponse(sesionJuegoId, tieneActivos, List.copyOf(acumulado.values()));
    }

    private Map<UUID, Implemento> loadImplementosMap() {
        Map<UUID, Implemento> map = new HashMap<>();
        implementoRepo.findAll().forEach(i -> map.put(i.getId(), i));
        return map;
    }

    private void registrarKardex(UUID implementoId, UUID prestamoId, String tipo, int cantidad,
                                 int stockAnterior, int stockNuevo, UUID usuarioId, String observacion) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setId(UUID.randomUUID());
        mov.setImplementoId(implementoId);
        mov.setPrestamoId(prestamoId);
        mov.setTipoMovimiento(tipo);
        mov.setCantidad(cantidad);
        mov.setStockAnterior(stockAnterior);
        mov.setStockNuevo(stockNuevo);
        mov.setUsuarioId(usuarioId);
        mov.setObservacion(observacion != null ? observacion : tipo);
        kardexRepo.save(mov);
    }

    private ImplementoResponse toImplementoResponse(Implemento i, String catCode) {
        return new ImplementoResponse(
                i.getId(), i.getCategoriaId(), catCode, i.getCodigo(), i.getNombre(),
                i.getStockTotal(), i.getStockDisponible(), i.getStockMantenimiento(),
                i.getCodigoTarifa(), i.isActivo()
        );
    }

    private PrestamoResponse toPrestamoResponse(Prestamo p, Map<UUID, Implemento> implementosMap) {
        List<DetallePrestamoResponse> dets = p.getDetalles().stream().map(d -> {
            Implemento imp = implementosMap.get(d.getImplementoId());
            return new DetallePrestamoResponse(
                    d.getId(), d.getImplementoId(),
                    imp != null ? imp.getNombre() : "Implemento",
                    imp != null ? imp.getCodigoTarifa() : "",
                    d.getCantidadEntregada(), d.getCantidadDevuelta(),
                    d.getCantidadDanada(), d.getCantidadPerdida(),
                    d.getEstadoRetorno(), d.getObservacionNovedad()
            );
        }).toList();
        return new PrestamoResponse(
                p.getId(), p.getSesionJuegoId(), p.getOperadorId(), p.getEstado(),
                p.getObservaciones(), p.getFechaPrestamo(), p.getFechaDevolucion(), dets
        );
    }
}
