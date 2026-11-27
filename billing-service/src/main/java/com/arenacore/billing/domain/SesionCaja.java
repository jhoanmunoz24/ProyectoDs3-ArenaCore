package com.arenacore.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sesiones_caja")
public class SesionCaja {

    @Id
    private UUID id;

    @Column(name = "caja_id", nullable = false)
    private UUID cajaId;

    @Column(name = "operador_id", nullable = false)
    private UUID operadorId;

    @Column(name = "saldo_inicial_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoInicialCop;

    @Column(name = "total_efectivo_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEfectivoCop = BigDecimal.ZERO;

    @Column(name = "total_transferencia_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalTransferenciaCop = BigDecimal.ZERO;

    @Column(name = "total_datafono_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDatafonoCop = BigDecimal.ZERO;

    @Column(name = "saldo_cierre_declarado_cop", precision = 12, scale = 2)
    private BigDecimal saldoCierreDeclaradoCop;

    @Column(name = "diferencia_arqueo_cop", precision = 12, scale = 2)
    private BigDecimal diferenciaArqueoCop;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "abierta_en", nullable = false)
    private Instant abiertaEn = Instant.now();

    @Column(name = "cerrada_en")
    private Instant cerradaEn;

    public SesionCaja() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCajaId() { return cajaId; }
    public void setCajaId(UUID cajaId) { this.cajaId = cajaId; }
    public UUID getOperadorId() { return operadorId; }
    public void setOperadorId(UUID operadorId) { this.operadorId = operadorId; }
    public BigDecimal getSaldoInicialCop() { return saldoInicialCop; }
    public void setSaldoInicialCop(BigDecimal saldoInicialCop) { this.saldoInicialCop = saldoInicialCop; }
    public BigDecimal getTotalEfectivoCop() { return totalEfectivoCop; }
    public void setTotalEfectivoCop(BigDecimal totalEfectivoCop) { this.totalEfectivoCop = totalEfectivoCop; }
    public BigDecimal getTotalTransferenciaCop() { return totalTransferenciaCop; }
    public void setTotalTransferenciaCop(BigDecimal totalTransferenciaCop) { this.totalTransferenciaCop = totalTransferenciaCop; }
    public BigDecimal getTotalDatafonoCop() { return totalDatafonoCop; }
    public void setTotalDatafonoCop(BigDecimal totalDatafonoCop) { this.totalDatafonoCop = totalDatafonoCop; }
    public BigDecimal getSaldoCierreDeclaradoCop() { return saldoCierreDeclaradoCop; }
    public void setSaldoCierreDeclaradoCop(BigDecimal saldoCierreDeclaradoCop) { this.saldoCierreDeclaradoCop = saldoCierreDeclaradoCop; }
    public BigDecimal getDiferenciaArqueoCop() { return diferenciaArqueoCop; }
    public void setDiferenciaArqueoCop(BigDecimal diferenciaArqueoCop) { this.diferenciaArqueoCop = diferenciaArqueoCop; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getAbiertaEn() { return abiertaEn; }
    public void setAbiertaEn(Instant abiertaEn) { this.abiertaEn = abiertaEn; }
    public Instant getCerradaEn() { return cerradaEn; }
    public void setCerradaEn(Instant cerradaEn) { this.cerradaEn = cerradaEn; }
}
