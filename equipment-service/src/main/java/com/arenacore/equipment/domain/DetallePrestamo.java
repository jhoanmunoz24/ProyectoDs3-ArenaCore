package com.arenacore.equipment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "detalles_prestamo")
public class DetallePrestamo {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestamo_id", nullable = false)
    private Prestamo prestamo;

    @Column(name = "implemento_id", nullable = false)
    private UUID implementoId;

    @Column(name = "cantidad_entregada", nullable = false)
    private int cantidadEntregada;

    @Column(name = "cantidad_devuelta", nullable = false)
    private int cantidadDevuelta;

    @Column(name = "cantidad_danada", nullable = false)
    private int cantidadDanada;

    @Column(name = "cantidad_perdida", nullable = false)
    private int cantidadPerdida;

    @Column(name = "estado_retorno", nullable = false, length = 25)
    private String estadoRetorno;

    @Column(name = "observacion_novedad", columnDefinition = "TEXT")
    private String observacionNovedad;

    public DetallePrestamo() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Prestamo getPrestamo() { return prestamo; }
    public void setPrestamo(Prestamo prestamo) { this.prestamo = prestamo; }
    public UUID getImplementoId() { return implementoId; }
    public void setImplementoId(UUID implementoId) { this.implementoId = implementoId; }
    public int getCantidadEntregada() { return cantidadEntregada; }
    public void setCantidadEntregada(int cantidadEntregada) { this.cantidadEntregada = cantidadEntregada; }
    public int getCantidadDevuelta() { return cantidadDevuelta; }
    public void setCantidadDevuelta(int cantidadDevuelta) { this.cantidadDevuelta = cantidadDevuelta; }
    public int getCantidadDanada() { return cantidadDanada; }
    public void setCantidadDanada(int cantidadDanada) { this.cantidadDanada = cantidadDanada; }
    public int getCantidadPerdida() { return cantidadPerdida; }
    public void setCantidadPerdida(int cantidadPerdida) { this.cantidadPerdida = cantidadPerdida; }
    public String getEstadoRetorno() { return estadoRetorno; }
    public void setEstadoRetorno(String estadoRetorno) { this.estadoRetorno = estadoRetorno; }
    public String getObservacionNovedad() { return observacionNovedad; }
    public void setObservacionNovedad(String observacionNovedad) { this.observacionNovedad = observacionNovedad; }
}
