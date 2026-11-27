package com.arenacore.equipment.repository;

import com.arenacore.equipment.domain.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PrestamoRepository extends JpaRepository<Prestamo, UUID> {
    List<Prestamo> findBySesionJuegoIdOrderByFechaPrestamoAsc(UUID sesionJuegoId);
}
