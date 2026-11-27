package com.arenacore.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tarifas_cancha")
public class TarifaCancha {

    @Id
    private UUID id;

    @Column(name = "tipo_cancha", nullable = false, length = 30)
    private String tipoCancha;

    @Column(nullable = false, length = 20)
    private String franja;

    @Column(name = "valor_hora_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorHoraCop;

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

    public TarifaCancha() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTipoCancha() { return tipoCancha; }
    public void setTipoCancha(String tipoCancha) { this.tipoCancha = tipoCancha; }
    public String getFranja() { return franja; }
    public void setFranja(String franja) { this.franja = franja; }
    public BigDecimal getValorHoraCop() { return valorHoraCop; }
    public void setValorHoraCop(BigDecimal valorHoraCop) { this.valorHoraCop = valorHoraCop; }
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
