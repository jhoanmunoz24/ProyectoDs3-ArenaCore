package com.arenacore.courts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bloques_ocupados")
public class BloqueOcupado {

    @Id
    private UUID id;

    @Column(name = "cancha_id", nullable = false)
    private UUID canchaId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "bloque_hora", nullable = false)
    private int bloqueHora;

    @Column(name = "reserva_id")
    private UUID reservaId;

    @Column(name = "sesion_juego_id")
    private UUID sesionJuegoId;

    @Column(nullable = false, length = 20)
    private String estado;

    public BloqueOcupado() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCanchaId() { return canchaId; }
    public void setCanchaId(UUID canchaId) { this.canchaId = canchaId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public int getBloqueHora() { return bloqueHora; }
    public void setBloqueHora(int bloqueHora) { this.bloqueHora = bloqueHora; }
    public UUID getReservaId() { return reservaId; }
    public void setReservaId(UUID reservaId) { this.reservaId = reservaId; }
    public UUID getSesionJuegoId() { return sesionJuegoId; }
    public void setSesionJuegoId(UUID sesionJuegoId) { this.sesionJuegoId = sesionJuegoId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
