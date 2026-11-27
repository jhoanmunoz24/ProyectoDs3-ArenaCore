package com.arenacore.courts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "canchas")
public class Cancha {

    @Id
    private UUID id;

    @Column(name = "sede_id", nullable = false)
    private UUID sedeId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(name = "capacidad_jugadores", nullable = false)
    private int capacidadJugadores;

    @Column(nullable = false, length = 80)
    private String superficie = "GRAMA_SINTETICA";

    @Column(name = "tiene_iluminacion", nullable = false)
    private boolean tieneIluminacion = true;

    @Column(name = "estado_operativo", nullable = false, length = 30)
    private String estadoOperativo = "HABILITADA";

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    public Cancha() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSedeId() { return sedeId; }
    public void setSedeId(UUID sedeId) { this.sedeId = sedeId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getCapacidadJugadores() { return capacidadJugadores; }
    public void setCapacidadJugadores(int capacidadJugadores) { this.capacidadJugadores = capacidadJugadores; }
    public String getSuperficie() { return superficie; }
    public void setSuperficie(String superficie) { this.superficie = superficie; }
    public boolean isTieneIluminacion() { return tieneIluminacion; }
    public void setTieneIluminacion(boolean tieneIluminacion) { this.tieneIluminacion = tieneIluminacion; }
    public String getEstadoOperativo() { return estadoOperativo; }
    public void setEstadoOperativo(String estadoOperativo) { this.estadoOperativo = estadoOperativo; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
