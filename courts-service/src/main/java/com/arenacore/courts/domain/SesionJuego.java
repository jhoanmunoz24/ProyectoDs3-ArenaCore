package com.arenacore.courts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sesiones_juego")
public class SesionJuego {

    @Id
    private UUID id;

    @Column(name = "reserva_id")
    private UUID reservaId;

    @Column(name = "cancha_id", nullable = false)
    private UUID canchaId;

    @Column(name = "nombre_capitan", nullable = false, length = 120)
    private String nombreCapitan;

    @Column(name = "telefono_capitan", nullable = false, length = 30)
    private String telefonoCapitan;

    @Column(name = "nombre_equipo", length = 120)
    private String nombreEquipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada", nullable = false)
    private Instant horaEntrada;

    @Column(name = "hora_salida_programada", nullable = false)
    private Instant horaSalidaProgramada;

    @Column(name = "hora_salida_real")
    private Instant horaSalidaReal;

    @Column(name = "duracion_horas_reservadas", nullable = false)
    private int duracionHorasReservadas;

    @Column(name = "liquidacion_id")
    private UUID liquidacionId;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(name = "operador_id", nullable = false)
    private UUID operadorId;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn = Instant.now();

    public SesionJuego() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getReservaId() { return reservaId; }
    public void setReservaId(UUID reservaId) { this.reservaId = reservaId; }
    public UUID getCanchaId() { return canchaId; }
    public void setCanchaId(UUID canchaId) { this.canchaId = canchaId; }
    public String getNombreCapitan() { return nombreCapitan; }
    public void setNombreCapitan(String nombreCapitan) { this.nombreCapitan = nombreCapitan; }
    public String getTelefonoCapitan() { return telefonoCapitan; }
    public void setTelefonoCapitan(String telefonoCapitan) { this.telefonoCapitan = telefonoCapitan; }
    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Instant getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(Instant horaEntrada) { this.horaEntrada = horaEntrada; }
    public Instant getHoraSalidaProgramada() { return horaSalidaProgramada; }
    public void setHoraSalidaProgramada(Instant horaSalidaProgramada) { this.horaSalidaProgramada = horaSalidaProgramada; }
    public Instant getHoraSalidaReal() { return horaSalidaReal; }
    public void setHoraSalidaReal(Instant horaSalidaReal) { this.horaSalidaReal = horaSalidaReal; }
    public int getDuracionHorasReservadas() { return duracionHorasReservadas; }
    public void setDuracionHorasReservadas(int duracionHorasReservadas) { this.duracionHorasReservadas = duracionHorasReservadas; }
    public UUID getLiquidacionId() { return liquidacionId; }
    public void setLiquidacionId(UUID liquidacionId) { this.liquidacionId = liquidacionId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public UUID getOperadorId() { return operadorId; }
    public void setOperadorId(UUID operadorId) { this.operadorId = operadorId; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
