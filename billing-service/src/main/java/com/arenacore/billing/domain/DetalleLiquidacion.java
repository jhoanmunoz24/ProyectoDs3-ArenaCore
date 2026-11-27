package com.arenacore.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "detalles_liquidacion")
public class DetalleLiquidacion {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquidacion_id", nullable = false)
    private Liquidacion liquidacion;

    @Column(nullable = false, length = 40)
    private String concepto;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "valor_unitario_snapshot_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitarioSnapshotCop;

    @Column(name = "subtotal_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalCop;

    public DetalleLiquidacion() {}

    public DetalleLiquidacion(Liquidacion liquidacion, String concepto, String descripcion,
                              int cantidad, BigDecimal valorUnitarioSnapshotCop, BigDecimal subtotalCop) {
        this.id = UUID.randomUUID();
        this.liquidacion = liquidacion;
        this.concepto = concepto;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.valorUnitarioSnapshotCop = valorUnitarioSnapshotCop;
        this.subtotalCop = subtotalCop;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Liquidacion getLiquidacion() { return liquidacion; }
    public void setLiquidacion(Liquidacion liquidacion) { this.liquidacion = liquidacion; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getValorUnitarioSnapshotCop() { return valorUnitarioSnapshotCop; }
    public void setValorUnitarioSnapshotCop(BigDecimal valorUnitarioSnapshotCop) { this.valorUnitarioSnapshotCop = valorUnitarioSnapshotCop; }
    public BigDecimal getSubtotalCop() { return subtotalCop; }
    public void setSubtotalCop(BigDecimal subtotalCop) { this.subtotalCop = subtotalCop; }
}
