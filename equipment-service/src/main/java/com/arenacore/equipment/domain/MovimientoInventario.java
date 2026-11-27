package com.arenacore.equipment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    @Id
    private UUID id;

    @Column(name = "implemento_id", nullable = false)
    private UUID implementoId;

    @Column(name = "prestamo_id")
    private UUID prestamoId;

    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private String tipoMovimiento;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private int stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private int stockNuevo;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, length = 255)
    private String observacion;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    public MovimientoInventario() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getImplementoId() { return implementoId; }
    public void setImplementoId(UUID implementoId) { this.implementoId = implementoId; }
    public UUID getPrestamoId() { return prestamoId; }
    public void setPrestamoId(UUID prestamoId) { this.prestamoId = prestamoId; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(int stockAnterior) { this.stockAnterior = stockAnterior; }
    public int getStockNuevo() { return stockNuevo; }
    public void setStockNuevo(int stockNuevo) { this.stockNuevo = stockNuevo; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
