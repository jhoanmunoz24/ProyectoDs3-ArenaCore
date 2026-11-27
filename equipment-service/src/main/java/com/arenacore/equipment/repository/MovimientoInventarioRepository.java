package com.arenacore.equipment.repository;

import com.arenacore.equipment.domain.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, UUID> {
    List<MovimientoInventario> findByImplementoIdOrderByCreadoEnDesc(UUID implementoId);
}
