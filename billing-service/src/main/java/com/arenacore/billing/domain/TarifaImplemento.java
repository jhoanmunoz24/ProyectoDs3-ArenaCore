package com.arenacore.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tarifas_implemento")
public class TarifaImplemento {

    @Id
    private UUID id;

    @Column(name = "codigo_tarifa", nullable = false, length = 60)
    private String codigoTarifa;

    @Column(name = "nombre_referencia", nullable = false, length = 120)
    private String nombreReferencia;

    @Column(name = "valor_alquiler_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAlquilerCop;

    @Column(name = "valor_reposicion_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorReposicionCop;

    @Column(name = "vigente_desde", nullable = false)
    private Instant vigenteDesde = Instant.now();

    @Column(name = "vigente_hasta")
    private Instant vigenteHasta;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "creado_por_id")
    private UUID creadoPorId;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    public TarifaImplemento() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCodigoTarifa() { return codigoTarifa; }
    public void setCodigoTarifa(String codigoTarifa) { this.codigoTarifa = codigoTarifa; }
    public String getNombreReferencia() { return nombreReferencia; }
    public void setNombreReferencia(String nombreReferencia) { this.nombreReferencia = nombreReferencia; }
    public BigDecimal getValorAlquilerCop() { return valorAlquilerCop; }
    public void setValorAlquilerCop(BigDecimal valorAlquilerCop) { this.valorAlquilerCop = valorAlquilerCop; }
    public BigDecimal getValorReposicionCop() { return valorReposicionCop; }
    public void setValorReposicionCop(BigDecimal valorReposicionCop) { this.valorReposicionCop = valorReposicionCop; }
    public Instant getVigenteDesde() { return vigenteDesde; }
    public void setVigenteDesde(Instant vigenteDesde) { this.vigenteDesde = vigenteDesde; }
    public Instant getVigenteHasta() { return vigenteHasta; }
    public void setVigenteHasta(Instant vigenteHasta) { this.vigenteHasta = vigenteHasta; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public UUID getCreadoPorId() { return creadoPorId; }
    public void setCreadoPorId(UUID creadoPorId) { this.creadoPorId = creadoPorId; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
