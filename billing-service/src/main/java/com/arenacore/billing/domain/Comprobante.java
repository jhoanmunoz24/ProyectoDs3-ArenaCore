package com.arenacore.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comprobantes")
public class Comprobante {

    @Id
    private UUID id;

    @Column(name = "numero_comprobante", nullable = false, unique = true, length = 60)
    private String numeroComprobante;

    @Column(name = "pago_id", nullable = false, unique = true)
    private UUID pagoId;

    @Column(name = "liquidacion_id", nullable = false)
    private UUID liquidacionId;

    @Column(name = "detalle_json", nullable = false, columnDefinition = "TEXT")
    private String detalleJson;

    @Column(name = "total_pagado_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPagadoCop;

    @Column(name = "emitido_en", nullable = false)
    private Instant emitidoEn = Instant.now();

    public Comprobante() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNumeroComprobante() { return numeroComprobante; }
    public void setNumeroComprobante(String numeroComprobante) { this.numeroComprobante = numeroComprobante; }
    public UUID getPagoId() { return pagoId; }
    public void setPagoId(UUID pagoId) { this.pagoId = pagoId; }
    public UUID getLiquidacionId() { return liquidacionId; }
    public void setLiquidacionId(UUID liquidacionId) { this.liquidacionId = liquidacionId; }
    public String getDetalleJson() { return detalleJson; }
    public void setDetalleJson(String detalleJson) { this.detalleJson = detalleJson; }
    public BigDecimal getTotalPagadoCop() { return totalPagadoCop; }
    public void setTotalPagadoCop(BigDecimal totalPagadoCop) { this.totalPagadoCop = totalPagadoCop; }
    public Instant getEmitidoEn() { return emitidoEn; }
    public void setEmitidoEn(Instant emitidoEn) { this.emitidoEn = emitidoEn; }
}
