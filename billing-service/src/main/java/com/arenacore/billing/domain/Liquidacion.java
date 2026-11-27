package com.arenacore.billing.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "liquidaciones")
public class Liquidacion {

    @Id
    private UUID id;

    @Column(name = "sesion_juego_id", nullable = false, unique = true)
    private UUID sesionJuegoId;

    @Column(name = "cancha_nombre", nullable = false, length = 120)
    private String canchaNombre;

    @Column(name = "tipo_cancha", nullable = false, length = 30)
    private String tipoCancha;

    @Column(name = "abono_previo_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal abonoPrevioCop = BigDecimal.ZERO;

    @Column(name = "subtotal_cancha_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalCanchaCop = BigDecimal.ZERO;

    @Column(name = "subtotal_implementos_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalImplementosCop = BigDecimal.ZERO;

    @Column(name = "subtotal_penalidades_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalPenalidadesCop = BigDecimal.ZERO;

    @Column(name = "total_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCop = BigDecimal.ZERO;

    @Column(name = "saldo_pendiente_cop", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoPendienteCop = BigDecimal.ZERO;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn = Instant.now();

    @OneToMany(mappedBy = "liquidacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleLiquidacion> detalles = new ArrayList<>();

    public Liquidacion() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSesionJuegoId() { return sesionJuegoId; }
    public void setSesionJuegoId(UUID sesionJuegoId) { this.sesionJuegoId = sesionJuegoId; }
    public String getCanchaNombre() { return canchaNombre; }
    public void setCanchaNombre(String canchaNombre) { this.canchaNombre = canchaNombre; }
    public String getTipoCancha() { return tipoCancha; }
    public void setTipoCancha(String tipoCancha) { this.tipoCancha = tipoCancha; }
    public BigDecimal getAbonoPrevioCop() { return abonoPrevioCop; }
    public void setAbonoPrevioCop(BigDecimal abonoPrevioCop) { this.abonoPrevioCop = abonoPrevioCop; }
    public BigDecimal getSubtotalCanchaCop() { return subtotalCanchaCop; }
    public void setSubtotalCanchaCop(BigDecimal subtotalCanchaCop) { this.subtotalCanchaCop = subtotalCanchaCop; }
    public BigDecimal getSubtotalImplementosCop() { return subtotalImplementosCop; }
    public void setSubtotalImplementosCop(BigDecimal subtotalImplementosCop) { this.subtotalImplementosCop = subtotalImplementosCop; }
    public BigDecimal getSubtotalPenalidadesCop() { return subtotalPenalidadesCop; }
    public void setSubtotalPenalidadesCop(BigDecimal subtotalPenalidadesCop) { this.subtotalPenalidadesCop = subtotalPenalidadesCop; }
    public BigDecimal getTotalCop() { return totalCop; }
    public void setTotalCop(BigDecimal totalCop) { this.totalCop = totalCop; }
    public BigDecimal getSaldoPendienteCop() { return saldoPendienteCop; }
    public void setSaldoPendienteCop(BigDecimal saldoPendienteCop) { this.saldoPendienteCop = saldoPendienteCop; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant actualizadoEn) { this.actualizadoEn = actualizadoEn; }
    public List<DetalleLiquidacion> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleLiquidacion> detalles) { this.detalles = detalles; }
}
