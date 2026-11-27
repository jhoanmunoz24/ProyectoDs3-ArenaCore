package com.arenacore.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    private UUID id;

    @Column(name = "liquidacion_id", nullable = false)
    private UUID liquidacionId;

    @Column(name = "sesion_caja_id", nullable = false)
    private UUID sesionCajaId;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(name = "referencia_transaccion", length = 120)
    private String referenciaTransaccion;

    @Column(name = "monto_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoCop;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "operador_id", nullable = false)
    private UUID operadorId;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    public Pago() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getLiquidacionId() { return liquidacionId; }
    public void setLiquidacionId(UUID liquidacionId) { this.liquidacionId = liquidacionId; }
    public UUID getSesionCajaId() { return sesionCajaId; }
    public void setSesionCajaId(UUID sesionCajaId) { this.sesionCajaId = sesionCajaId; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getReferenciaTransaccion() { return referenciaTransaccion; }
    public void setReferenciaTransaccion(String referenciaTransaccion) { this.referenciaTransaccion = referenciaTransaccion; }
    public BigDecimal getMontoCop() { return montoCop; }
    public void setMontoCop(BigDecimal montoCop) { this.montoCop = montoCop; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public UUID getOperadorId() { return operadorId; }
    public void setOperadorId(UUID operadorId) { this.operadorId = operadorId; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
