package com.arenacore.courts.service;

import com.arenacore.courts.domain.*;
import com.arenacore.courts.dto.CourtsDtos.*;
import com.arenacore.courts.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class CourtsCoreService {

    public static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");
    private static final int HORA_APERTURA = 7;
    private static final int HORA_CIERRE = 23;
    private static final int INICIO_NOCTURNA = 18;
    private static final int MINUTOS_GRACIA = 10;

    private final SedeRepository sedeRepo;
    private final CanchaRepository canchaRepo;
    private final MantenimientoCanchaRepository mantenimientoRepo;
    private final ReservaRepository reservaRepo;
    private final SesionJuegoRepository sesionRepo;
    private final EventoSesionJuegoRepository eventoRepo;
    private final BloqueOcupadoRepository bloqueRepo;
    private final IdempotencyService idempotencyService;

    public CourtsCoreService(
            SedeRepository sedeRepo,
            CanchaRepository canchaRepo,
            MantenimientoCanchaRepository mantenimientoRepo,
            ReservaRepository reservaRepo,
            SesionJuegoRepository sesionRepo,
            EventoSesionJuegoRepository eventoRepo,
            BloqueOcupadoRepository bloqueRepo,
            IdempotencyService idempotencyService) {
        this.sedeRepo = sedeRepo;
        this.canchaRepo = canchaRepo;
        this.mantenimientoRepo = mantenimientoRepo;
        this.reservaRepo = reservaRepo;
        this.sesionRepo = sesionRepo;
        this.eventoRepo = eventoRepo;
        this.bloqueRepo = bloqueRepo;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public SedeResponse crearSede(SedeCreateRequest request) {
        Sede sede = new Sede();
        sede.setId(UUID.randomUUID());
        sede.setNombre(request.nombre().trim());
        sede.setDireccion(request.direccion().trim());
        sede.setTelefono(request.telefono());
        sedeRepo.save(sede);
        return toSedeResponse(sede);
    }

    @Transactional(readOnly = true)
    public List<SedeResponse> listarSedes() {
        return sedeRepo.findAll().stream().map(this::toSedeResponse).toList();
    }

    @Transactional
    public CanchaResponse crearCancha(CanchaCreateRequest request) {
        sedeRepo.findById(request.sedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada"));

        String tipo = request.tipo().trim().toUpperCase();
        if (!List.of("FUTBOL_5", "FUTBOL_7", "FUTBOL_8", "FUTBOL_11").contains(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de cancha inválido (FUTBOL_5, FUTBOL_7, FUTBOL_8, FUTBOL_11)");
        }

        Cancha c = new Cancha();
        c.setId(UUID.randomUUID());
        c.setSedeId(request.sedeId());
        c.setNombre(request.nombre().trim());
        c.setTipo(tipo);
        c.setCapacidadJugadores(request.capacidadJugadores());
        c.setSuperficie(request.superficie() != null ? request.superficie() : "GRAMA_SINTETICA");
        c.setTieneIluminacion(request.tieneIluminacion() == null || request.tieneIluminacion());
        c.setEstadoOperativo("HABILITADA");
        canchaRepo.save(c);
        return toCanchaResponse(c);
    }

    @Transactional(readOnly = true)
    public List<CanchaResponse> listarCanchas(UUID sedeId) {
        List<Cancha> lista = sedeId != null ? canchaRepo.findBySedeIdOrderByNombreAsc(sedeId) : canchaRepo.findAll();
        return lista.stream().map(this::toCanchaResponse).toList();
    }

    @Transactional
    public CanchaResponse actualizarEstadoCancha(UUID canchaId, CanchaEstadoRequest request) {
        Cancha c = canchaRepo.findById(canchaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cancha no encontrada"));
        String estado = request.estadoOperativo().trim().toUpperCase();
        if (!List.of("HABILITADA", "MANTENIMIENTO", "INACTIVA").contains(estado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado operativo inválido");
        }
        c.setEstadoOperativo(estado);
        canchaRepo.save(c);
        return toCanchaResponse(c);
    }

    @Transactional
    public MantenimientoCancha programarMantenimiento(UUID canchaId, MantenimientoRequest request) {
        canchaRepo.findById(canchaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cancha no encontrada"));
        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de fin debe ser posterior a la hora de inicio");
        }
        MantenimientoCancha m = new MantenimientoCancha();
        m.setId(UUID.randomUUID());
        m.setCanchaId(canchaId);
        m.setFecha(request.fecha());
        m.setHoraInicio(request.horaInicio());
        m.setHoraFin(request.horaFin());
        m.setMotivo(request.motivo().trim());
        m.setEstado("PROGRAMADO");
        return mantenimientoRepo.save(m);
    }

    @Transactional(readOnly = true)
    public DisponibilidadDiaResponse consultarDisponibilidad(LocalDate fecha) {
        List<Cancha> canchas = canchaRepo.findAll();
        List<BloqueOcupado> bloquesDia = bloqueRepo.findByFechaAndEstadoIn(fecha, List.of("CONFIRMADA", "ACTIVA"));
        List<MantenimientoCancha> mantenimientos = mantenimientoRepo.findByFechaAndEstadoIn(
                fecha, List.of("PROGRAMADO", "EN_CURSO")
        );

        Map<UUID, Map<Integer, String>> ocupacionPorCancha = new HashMap<>();
        for (BloqueOcupado b : bloquesDia) {
            String estadoSlot = "ACTIVA".equals(b.getEstado()) ? "OCUPADA" : "RESERVADA";
            ocupacionPorCancha.computeIfAbsent(b.getCanchaId(), k -> new HashMap<>())
                    .put(b.getBloqueHora(), estadoSlot);
        }

        Map<UUID, Set<Integer>> mantPorCancha = new HashMap<>();
        for (MantenimientoCancha m : mantenimientos) {
            Set<Integer> horas = mantPorCancha.computeIfAbsent(m.getCanchaId(), k -> new HashSet<>());
            for (int h = m.getHoraInicio().getHour(); h < m.getHoraFin().getHour(); h++) {
                horas.add(h);
            }
        }

        List<CanchaDisponibilidadResponse> grid = new ArrayList<>();
        for (Cancha cancha : canchas) {
            Map<Integer, String> ocupados = ocupacionPorCancha.getOrDefault(cancha.getId(), Map.of());
            Set<Integer> mantHoras = mantPorCancha.getOrDefault(cancha.getId(), Set.of());
            List<BloqueDisponibilidadResponse> slots = new ArrayList<>();

            for (int hora = HORA_APERTURA; hora < HORA_CIERRE; hora++) {
                String franja = hora >= INICIO_NOCTURNA ? "NOCTURNA" : "DIURNA";
                String estado;
                if (!"HABILITADA".equals(cancha.getEstadoOperativo()) || mantHoras.contains(hora)) {
                    estado = "MANTENIMIENTO";
                } else {
                    estado = ocupados.getOrDefault(hora, "DISPONIBLE");
                }
                slots.add(new BloqueDisponibilidadResponse(hora, franja, estado));
            }
            grid.add(new CanchaDisponibilidadResponse(
                    cancha.getId(), cancha.getNombre(), cancha.getTipo(), cancha.getEstadoOperativo(), slots
            ));
        }

        return new DisponibilidadDiaResponse(fecha, grid);
    }

    @Transactional
    public ResponseEntity<String> crearReserva(String rawKey, CrearReservaRequest request, UUID actorId) {
        String key = idempotencyService.requireKey(rawKey);
        String endpoint = "POST:/api/v1/reservas";
        Optional<ResponseEntity<String>> replay = idempotencyService.findReplay(key, endpoint, request);
        if (replay.isPresent()) {
            return replay.get();
        }

        Cancha cancha = canchaRepo.findById(request.canchaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cancha no encontrada"));
        validarHorarioYEstadoCancha(cancha, request.fecha(), request.horaInicio(), request.duracionHoras());

        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setCodigoReserva("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reserva.setCanchaId(cancha.getId());
        reserva.setClienteId(actorId);
        reserva.setNombreCapitan(request.nombreCapitan().trim());
        reserva.setTelefonoCapitan(request.telefonoCapitan().trim());
        reserva.setNombreEquipo(request.nombreEquipo());
        reserva.setFecha(request.fecha());
        reserva.setHoraInicio(request.horaInicio());
        reserva.setHoraFin(request.horaInicio().plusHours(request.duracionHoras()));
        reserva.setDuracionHoras(request.duracionHoras());
        reserva.setEstado("CONFIRMADA");
        reserva.setAbonoCop(request.abonoCop() != null ? request.abonoCop() : BigDecimal.ZERO);
        reserva.setCreadoPorId(actorId);
        reservaRepo.save(reserva);

        ocuparBloques(cancha.getId(), request.fecha(), request.horaInicio().getHour(),
                request.duracionHoras(), reserva.getId(), null, "CONFIRMADA");

        ReservaResponse res = toReservaResponse(reserva, null);
        return idempotencyService.saveAndBuildResponse(key, endpoint, request, HttpStatus.CREATED, res);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarReservas(LocalDate fecha) {
        List<Reserva> list = fecha != null ? reservaRepo.findByFechaOrderByHoraInicioAsc(fecha) : reservaRepo.findAll();
        return list.stream().map(r -> toReservaResponse(r, null)).toList();
    }

    @Transactional(readOnly = true)
    public Reserva obtenerReservaEntity(UUID reservaId) {
        return reservaRepo.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
    }

    @Transactional
    public ReservaResponse cancelarReservaLocal(UUID reservaId, CancelarReservaRequest request, boolean exoneradaPorAutorizacion) {
        Reserva reserva = reservaRepo.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
        if (!"CONFIRMADA".equals(reserva.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden cancelar reservas en estado CONFIRMADA (estado actual: " + reserva.getEstado() + ")");
        }

        ZonedDateTime inicioPartido = ZonedDateTime.of(reserva.getFecha(), reserva.getHoraInicio(), BOGOTA_ZONE);
        long horasAnticipacion = Duration.between(ZonedDateTime.now(BOGOTA_ZONE), inicioPartido).toHours();
        int cargoPorcentaje = (horasAnticipacion < 4 && !exoneradaPorAutorizacion) ? 30 : 0;

        reserva.setEstado("CANCELADA");
        reserva.setMotivoCancelacion(request.motivo());
        reserva.setAutorizacionExcepcionalId(request.autorizacionExcepcionalId());
        reserva.setActualizadoEn(Instant.now());
        reservaRepo.save(reserva);

        List<BloqueOcupado> bloques = bloqueRepo.findByReservaId(reservaId);
        bloques.forEach(b -> b.setEstado("LIBERADA"));
        bloqueRepo.saveAll(bloques);

        return toReservaResponse(reserva, cargoPorcentaje);
    }

    @Transactional
    public ResponseEntity<String> registrarCheckIn(String rawKey, CheckInRequest request, UUID operadorId) {
        String key = idempotencyService.requireKey(rawKey);
        String endpoint = "POST:/api/v1/sesiones-juego/checkin";
        Optional<ResponseEntity<String>> replay = idempotencyService.findReplay(key, endpoint, request);
        if (replay.isPresent()) {
            return replay.get();
        }

        SesionJuego sesion = new SesionJuego();
        sesion.setId(UUID.randomUUID());
        sesion.setOperadorId(operadorId);
        sesion.setEstado("ACTIVA");

        Cancha cancha;
        LocalDate fecha;
        LocalTime horaInicio;
        int duracionHoras;

        if (request.reservaId() != null) {
            Reserva reserva = reservaRepo.findById(request.reservaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
            if (!"CONFIRMADA".equals(reserva.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La reserva no está en estado CONFIRMADA (estado: " + reserva.getEstado() + ")");
            }
            cancha = canchaRepo.findById(reserva.getCanchaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cancha no encontrada"));
            fecha = reserva.getFecha();
            horaInicio = reserva.getHoraInicio();
            duracionHoras = reserva.getDuracionHoras();

            sesion.setReservaId(reserva.getId());
            sesion.setCanchaId(cancha.getId());
            sesion.setNombreCapitan(reserva.getNombreCapitan());
            sesion.setTelefonoCapitan(reserva.getTelefonoCapitan());
            sesion.setNombreEquipo(reserva.getNombreEquipo());
            sesion.setFecha(fecha);
            sesion.setDuracionHorasReservadas(duracionHoras);
            sesion.setHoraEntrada(Instant.now());
            sesion.setHoraSalidaProgramada(ZonedDateTime.of(fecha, horaInicio.plusHours(duracionHoras), BOGOTA_ZONE).toInstant());
            sesionRepo.save(sesion);

            reserva.setEstado("CHECK_IN_REALIZADO");
            reserva.setActualizadoEn(Instant.now());
            reservaRepo.save(reserva);

            List<BloqueOcupado> bloques = bloqueRepo.findByReservaId(reserva.getId());
            for (BloqueOcupado b : bloques) {
                b.setEstado("ACTIVA");
                b.setSesionJuegoId(sesion.getId());
            }
            bloqueRepo.saveAll(bloques);
        } else {
            if (request.canchaId() == null || request.nombreCapitan() == null || request.telefonoCapitan() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Para ingreso directo sin reserva debe indicar canchaId, nombreCapitan y telefonoCapitan");
            }
            cancha = canchaRepo.findById(request.canchaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cancha no encontrada"));

            ZonedDateTime ahora = ZonedDateTime.now(BOGOTA_ZONE);
            fecha = request.fecha() != null ? request.fecha() : ahora.toLocalDate();
            horaInicio = request.horaInicio() != null
                    ? request.horaInicio()
                    : LocalTime.of(Math.max(HORA_APERTURA, Math.min(HORA_CIERRE - 1, ahora.getHour())), 0);
            duracionHoras = request.duracionHoras() != null ? request.duracionHoras() : 1;

            validarHorarioYEstadoCancha(cancha, fecha, horaInicio, duracionHoras);

            sesion.setCanchaId(cancha.getId());
            sesion.setNombreCapitan(request.nombreCapitan().trim());
            sesion.setTelefonoCapitan(request.telefonoCapitan().trim());
            sesion.setNombreEquipo(request.nombreEquipo());
            sesion.setFecha(fecha);
            sesion.setDuracionHorasReservadas(duracionHoras);
            sesion.setHoraEntrada(Instant.now());
            sesion.setHoraSalidaProgramada(ZonedDateTime.of(fecha, horaInicio.plusHours(duracionHoras), BOGOTA_ZONE).toInstant());
            sesionRepo.save(sesion);

            ocuparBloques(cancha.getId(), fecha, horaInicio.getHour(), duracionHoras, null, sesion.getId(), "ACTIVA");
        }

        registrarEvento(sesion.getId(), "CHECK_IN",
                "Ingreso a cancha " + cancha.getNombre() + " registrado", null, cancha.getId(), operadorId);

        SesionJuegoResponse res = toSesionResponse(sesion, cancha);
        return idempotencyService.saveAndBuildResponse(key, endpoint, request, HttpStatus.CREATED, res);
    }

    @Transactional(readOnly = true)
    public List<SesionJuegoResponse> listarSesiones(boolean soloActivas) {
        Map<UUID, Cancha> canchasMap = new HashMap<>();
        canchaRepo.findAll().forEach(c -> canchasMap.put(c.getId(), c));

        List<SesionJuego> sesiones = soloActivas
                ? sesionRepo.findByEstadoInOrderByHoraEntradaAsc(List.of("ACTIVA", "LIQUIDACION_SOLICITADA"))
                : sesionRepo.findAll();

        return sesiones.stream()
                .map(s -> toSesionResponse(s, canchasMap.get(s.getCanchaId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public SesionJuegoResponse obtenerSesion(UUID sesionId) {
        SesionJuego s = sesionRepo.findById(sesionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de juego no encontrada"));
        Cancha cancha = canchaRepo.findById(s.getCanchaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cancha no encontrada"));
        return toSesionResponse(s, cancha);
    }

    @Transactional(readOnly = true)
    public DatosLiquidacionSesion obtenerDatosParaLiquidacion(UUID sesionId) {
        SesionJuego s = sesionRepo.findById(sesionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de juego no encontrada"));
        if ("CERRADA".equals(s.getEstado()) || "ANULADA".equals(s.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión ya está " + s.getEstado());
        }
        Cancha cancha = canchaRepo.findById(s.getCanchaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cancha no encontrada"));

        LocalTime horaInicio = s.getHoraSalidaProgramada()
                .atZone(BOGOTA_ZONE)
                .toLocalTime()
                .minusHours(s.getDuracionHorasReservadas());

        int[] split = calcularHorasDiurnasYNocturnas(horaInicio.getHour(), s.getDuracionHorasReservadas());
        int horasAdicionales = calcularHorasAdicionalesPorGracia(s.getHoraSalidaProgramada(), Instant.now());
        int horaExtra = (horaInicio.getHour() + s.getDuracionHorasReservadas()) % 24;
        String franjaExtra = horaExtra >= INICIO_NOCTURNA ? "NOCTURNA" : "DIURNA";

        BigDecimal abono = BigDecimal.ZERO;
        if (s.getReservaId() != null) {
            abono = reservaRepo.findById(s.getReservaId()).map(Reserva::getAbonoCop).orElse(BigDecimal.ZERO);
        }

        return new DatosLiquidacionSesion(
                toSesionResponse(s, cancha),
                split[0],
                split[1],
                horasAdicionales,
                franjaExtra,
                abono
        );
    }

    @Transactional
    public SesionJuegoResponse registrarLiquidacionEnSesion(UUID sesionId, UUID liquidacionId, UUID actorId) {
        SesionJuego s = sesionRepo.findById(sesionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de juego no encontrada"));
        s.setLiquidacionId(liquidacionId);
        s.setEstado("LIQUIDACION_SOLICITADA");
        s.setActualizadoEn(Instant.now());
        sesionRepo.save(s);

        registrarEvento(sesionId, "SOLICITUD_LIQUIDACION",
                "Liquidación consolidada generada en Billing: " + liquidacionId, null, null, actorId);

        Cancha cancha = canchaRepo.findById(s.getCanchaId()).orElse(null);
        return toSesionResponse(s, cancha);
    }

    @Transactional
    public SesionJuegoResponse cerrarSesionLocal(UUID sesionId, UUID actorId) {
        SesionJuego s = sesionRepo.findById(sesionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de juego no encontrada"));
        if ("CERRADA".equals(s.getEstado())) {
            Cancha c = canchaRepo.findById(s.getCanchaId()).orElse(null);
            return toSesionResponse(s, c);
        }

        s.setEstado("CERRADA");
        s.setHoraSalidaReal(Instant.now());
        s.setActualizadoEn(Instant.now());
        sesionRepo.save(s);

        List<BloqueOcupado> bloques = bloqueRepo.findBySesionJuegoId(sesionId);
        bloques.forEach(b -> b.setEstado("LIBERADA"));
        bloqueRepo.saveAll(bloques);

        registrarEvento(sesionId, "CIERRE_SESION",
                "Sesión cerrada tras confirmar pago total en Billing; cancha liberada", null, null, actorId);

        Cancha cancha = canchaRepo.findById(s.getCanchaId()).orElse(null);
        return toSesionResponse(s, cancha);
    }

    public static int[] calcularHorasDiurnasYNocturnas(int horaInicio, int duracionHoras) {
        int diurnas = 0;
        int nocturnas = 0;
        for (int i = 0; i < duracionHoras; i++) {
            int hora = horaInicio + i;
            if (hora >= INICIO_NOCTURNA) {
                nocturnas++;
            } else {
                diurnas++;
            }
        }
        return new int[]{diurnas, nocturnas};
    }

    public static int calcularHorasAdicionalesPorGracia(Instant horaSalidaProgramada, Instant horaActual) {
        long minutosExcedidos = Duration.between(horaSalidaProgramada, horaActual).toMinutes();
        if (minutosExcedidos <= MINUTOS_GRACIA) {
            return 0;
        }
        return (int) Math.ceil((minutosExcedidos - MINUTOS_GRACIA) / 60.0);
    }

    private void validarHorarioYEstadoCancha(Cancha cancha, LocalDate fecha, LocalTime horaInicio, int duracionHoras) {
        if (!"HABILITADA".equals(cancha.getEstadoOperativo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La cancha '" + cancha.getNombre() + "' no está HABILITADA (estado: " + cancha.getEstadoOperativo() + ")");
        }
        if (horaInicio.getMinute() != 0 || horaInicio.getSecond() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las reservas deben iniciar en punto (bloques exactos de 60 minutos)");
        }
        int hInicio = horaInicio.getHour();
        if (hInicio < HORA_APERTURA || hInicio + duracionHoras > HORA_CIERRE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Horario fuera de operación permitida (07:00 a 23:00)");
        }

        List<MantenimientoCancha> mants = mantenimientoRepo.findByCanchaIdAndFechaAndEstadoIn(
                cancha.getId(), fecha, List.of("PROGRAMADO", "EN_CURSO")
        );
        LocalTime horaFin = horaInicio.plusHours(duracionHoras);
        for (MantenimientoCancha m : mants) {
            if (horaInicio.isBefore(m.getHoraFin()) && horaFin.isAfter(m.getHoraInicio())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La cancha tiene mantenimiento programado en ese horario: " + m.getMotivo());
            }
        }

        for (int i = 0; i < duracionHoras; i++) {
            int bloque = hInicio + i;
            if (bloqueRepo.existsByCanchaIdAndFechaAndBloqueHoraAndEstadoIn(
                    cancha.getId(), fecha, bloque, List.of("CONFIRMADA", "ACTIVA"))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Overbooking prevenido: la cancha '" + cancha.getNombre() + "' ya está ocupada a las " + bloque + ":00");
            }
        }
    }

    private void ocuparBloques(UUID canchaId, LocalDate fecha, int horaInicio, int duracionHoras,
                               UUID reservaId, UUID sesionId, String estado) {
        for (int i = 0; i < duracionHoras; i++) {
            BloqueOcupado bloque = new BloqueOcupado();
            bloque.setId(UUID.randomUUID());
            bloque.setCanchaId(canchaId);
            bloque.setFecha(fecha);
            bloque.setBloqueHora(horaInicio + i);
            bloque.setReservaId(reservaId);
            bloque.setSesionJuegoId(sesionId);
            bloque.setEstado(estado);
            bloqueRepo.save(bloque);
        }
    }

    private void registrarEvento(UUID sesionId, String tipo, String desc, UUID anterior, UUID nueva, UUID usuarioId) {
        EventoSesionJuego ev = new EventoSesionJuego();
        ev.setId(UUID.randomUUID());
        ev.setSesionJuegoId(sesionId);
        ev.setTipoEvento(tipo);
        ev.setDescripcion(desc);
        ev.setCanchaAnteriorId(anterior);
        ev.setCanchaNuevaId(nueva);
        ev.setUsuarioId(usuarioId);
        eventoRepo.save(ev);
    }

    private SedeResponse toSedeResponse(Sede s) {
        return new SedeResponse(s.getId(), s.getNombre(), s.getDireccion(), s.getTelefono(),
                s.getHoraApertura(), s.getHoraCierre(), s.getZonaHoraria(), s.isActivo());
    }

    private CanchaResponse toCanchaResponse(Cancha c) {
        return new CanchaResponse(c.getId(), c.getSedeId(), c.getNombre(), c.getTipo(),
                c.getCapacidadJugadores(), c.getSuperficie(), c.isTieneIluminacion(), c.getEstadoOperativo());
    }

    private ReservaResponse toReservaResponse(Reserva r, Integer cargoPorcentaje) {
        return new ReservaResponse(r.getId(), r.getCodigoReserva(), r.getCanchaId(), r.getNombreCapitan(),
                r.getTelefonoCapitan(), r.getNombreEquipo(), r.getFecha(), r.getHoraInicio(),
                r.getHoraFin(), r.getDuracionHoras(), r.getEstado(), r.getAbonoCop(), cargoPorcentaje, r.getCreadoEn());
    }

    private SesionJuegoResponse toSesionResponse(SesionJuego s, Cancha c) {
        Instant finRef = s.getHoraSalidaReal() != null ? s.getHoraSalidaReal() : Instant.now();
        long mins = Math.max(0, Duration.between(s.getHoraEntrada(), finRef).toMinutes());
        return new SesionJuegoResponse(
                s.getId(), s.getReservaId(), s.getCanchaId(),
                c != null ? c.getNombre() : "Cancha",
                c != null ? c.getTipo() : "FUTBOL_5",
                s.getNombreCapitan(), s.getTelefonoCapitan(), s.getNombreEquipo(),
                s.getFecha(), s.getHoraEntrada(), s.getHoraSalidaProgramada(), s.getHoraSalidaReal(),
                s.getDuracionHorasReservadas(), s.getLiquidacionId(), s.getEstado(), mins
        );
    }
}
