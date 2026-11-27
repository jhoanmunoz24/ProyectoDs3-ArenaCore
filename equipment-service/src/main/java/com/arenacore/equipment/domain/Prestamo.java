package com.arenacore.equipment.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    private UUID id;

    @Column(name = "sesion_juego_id", nullable = false)
    private UUID sesionJuegoId;

    @Column(name = "operador_id", nullable = false)
    private UUID operadorId;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_prestamo", nullable = false)
    private Instant fechaPrestamo = Instant.now();

    @Column(name = "fecha_devolucion")
    private Instant fechaDevolucion;

    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetallePrestamo> detalles = new ArrayList<>();

    public Prestamo() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSesionJuegoId() { return sesionJuegoId; }
    public void setSesionJuegoId(UUID sesionJuegoId) { this.sesionJuegoId = sesionJuegoId; }
    public UUID getOperadorId() { return operadorId; }
    public void setOperadorId(UUID operadorId) { this.operadorId = operadorId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Instant getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(Instant fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    public Instant getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(Instant fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public List<DetallePrestamo> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePrestamo> detalles) { this.detalles = detalles; }
}
