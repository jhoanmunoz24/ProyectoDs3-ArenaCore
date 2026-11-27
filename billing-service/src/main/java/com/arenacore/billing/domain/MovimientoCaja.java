package com.arenacore.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja {

    @Id
    private UUID id;

    @Column(name = "sesion_caja_id", nullable = false)
    private UUID sesionCajaId;

    @Column(name = "pago_id", nullable = false)
    private UUID pagoId;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(name = "monto_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoCop;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    public MovimientoCaja() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSesionCajaId() { return sesionCajaId; }
    public void setSesionCajaId(UUID sesionCajaId) { this.sesionCajaId = sesionCajaId; }
    public UUID getPagoId() { return pagoId; }
    public void setPagoId(UUID pagoId) { this.pagoId = pagoId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public BigDecimal getMontoCop() { return montoCop; }
    public void setMontoCop(BigDecimal montoCop) { this.montoCop = montoCop; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
