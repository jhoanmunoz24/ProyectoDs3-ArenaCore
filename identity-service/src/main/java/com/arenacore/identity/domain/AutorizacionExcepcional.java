package com.arenacore.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "autorizaciones_excepcionales")
public class AutorizacionExcepcional {

    @Id
    private UUID id;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(name = "referencia_externa_id", nullable = false)
    private UUID referenciaExternaId;

    @Column(name = "solicitado_por_id", nullable = false)
    private UUID solicitadoPorId;

    @Column(name = "aprobado_por_id")
    private UUID aprobadoPorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(name = "observacion_resolucion", columnDefinition = "TEXT")
    private String observacionResolucion;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    @Column(name = "resuelto_en")
    private Instant resueltoEn;

    public AutorizacionExcepcional() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public UUID getReferenciaExternaId() { return referenciaExternaId; }
    public void setReferenciaExternaId(UUID referenciaExternaId) { this.referenciaExternaId = referenciaExternaId; }
    public UUID getSolicitadoPorId() { return solicitadoPorId; }
    public void setSolicitadoPorId(UUID solicitadoPorId) { this.solicitadoPorId = solicitadoPorId; }
    public UUID getAprobadoPorId() { return aprobadoPorId; }
    public void setAprobadoPorId(UUID aprobadoPorId) { this.aprobadoPorId = aprobadoPorId; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacionResolucion() { return observacionResolucion; }
    public void setObservacionResolucion(String observacionResolucion) { this.observacionResolucion = observacionResolucion; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Instant getResueltoEn() { return resueltoEn; }
    public void setResueltoEn(Instant resueltoEn) { this.resueltoEn = resueltoEn; }
}
