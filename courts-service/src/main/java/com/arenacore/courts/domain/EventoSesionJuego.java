package com.arenacore.courts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eventos_sesion_juego")
public class EventoSesionJuego {

    @Id
    private UUID id;

    @Column(name = "sesion_juego_id", nullable = false)
    private UUID sesionJuegoId;

    @Column(name = "tipo_evento", nullable = false, length = 40)
    private String tipoEvento;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(name = "cancha_anterior_id")
    private UUID canchaAnteriorId;

    @Column(name = "cancha_nueva_id")
    private UUID canchaNuevaId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    public EventoSesionJuego() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSesionJuegoId() { return sesionJuegoId; }
    public void setSesionJuegoId(UUID sesionJuegoId) { this.sesionJuegoId = sesionJuegoId; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public UUID getCanchaAnteriorId() { return canchaAnteriorId; }
    public void setCanchaAnteriorId(UUID canchaAnteriorId) { this.canchaAnteriorId = canchaAnteriorId; }
    public UUID getCanchaNuevaId() { return canchaNuevaId; }
    public void setCanchaNuevaId(UUID canchaNuevaId) { this.canchaNuevaId = canchaNuevaId; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
