package com.arenacore.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auditoria_seguridad")
public class AuditoriaSeguridad {

    @Id
    private UUID id;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "email_intento", length = 150)
    private String emailIntento;

    @Column(nullable = false, length = 60)
    private String evento;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "ip_origen", length = 80)
    private String ipOrigen;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    public AuditoriaSeguridad() {}

    public AuditoriaSeguridad(UUID usuarioId, String emailIntento, String evento, String detalle, String ipOrigen) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.emailIntento = emailIntento;
        this.evento = evento;
        this.detalle = detalle;
        this.ipOrigen = ipOrigen;
        this.creadoEn = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public String getEmailIntento() { return emailIntento; }
    public void setEmailIntento(String emailIntento) { this.emailIntento = emailIntento; }
    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
