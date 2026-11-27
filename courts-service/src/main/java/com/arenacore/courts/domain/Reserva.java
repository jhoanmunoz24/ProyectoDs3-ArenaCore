package com.arenacore.courts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    private UUID id;

    @Column(name = "codigo_reserva", nullable = false, unique = true, length = 40)
    private String codigoReserva;

    @Column(name = "cancha_id", nullable = false)
    private UUID canchaId;

    @Column(name = "cliente_id")
    private UUID clienteId;

    @Column(name = "nombre_capitan", nullable = false, length = 120)
    private String nombreCapitan;

    @Column(name = "telefono_capitan", nullable = false, length = 30)
    private String telefonoCapitan;

    @Column(name = "nombre_equipo", length = 120)
    private String nombreEquipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "duracion_horas", nullable = false)
    private int duracionHoras;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(name = "abono_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal abonoCop = BigDecimal.ZERO;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Column(name = "autorizacion_excepcional_id")
    private UUID autorizacionExcepcionalId;

    @Column(name = "creado_por_id", nullable = false)
    private UUID creadoPorId;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn = Instant.now();

    public Reserva() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCodigoReserva() { return codigoReserva; }
    public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }
    public UUID getCanchaId() { return canchaId; }
    public void setCanchaId(UUID canchaId) { this.canchaId = canchaId; }
    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }
    public String getNombreCapitan() { return nombreCapitan; }
    public void setNombreCapitan(String nombreCapitan) { this.nombreCapitan = nombreCapitan; }
    public String getTelefonoCapitan() { return telefonoCapitan; }
    public void setTelefonoCapitan(String telefonoCapitan) { this.telefonoCapitan = telefonoCapitan; }
    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public int getDuracionHoras() { return duracionHoras; }
    public void setDuracionHoras(int duracionHoras) { this.duracionHoras = duracionHoras; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getAbonoCop() { return abonoCop; }
    public void setAbonoCop(BigDecimal abonoCop) { this.abonoCop = abonoCop; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public UUID getAutorizacionExcepcionalId() { return autorizacionExcepcionalId; }
    public void setAutorizacionExcepcionalId(UUID autorizacionExcepcionalId) { this.autorizacionExcepcionalId = autorizacionExcepcionalId; }
    public UUID getCreadoPorId() { return creadoPorId; }
    public void setCreadoPorId(UUID creadoPorId) { this.creadoPorId = creadoPorId; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
