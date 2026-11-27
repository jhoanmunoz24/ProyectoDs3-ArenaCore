package com.arenacore.courts.repository;

import com.arenacore.courts.domain.MantenimientoCancha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MantenimientoCanchaRepository extends JpaRepository<MantenimientoCancha, UUID> {
    List<MantenimientoCancha> findByFechaAndEstadoIn(LocalDate fecha, List<String> estados);
    List<MantenimientoCancha> findByCanchaIdAndFechaAndEstadoIn(UUID canchaId, LocalDate fecha, List<String> estados);
}
