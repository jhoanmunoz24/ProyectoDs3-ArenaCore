package com.arenacore.billing.repository;

import com.arenacore.billing.domain.Liquidacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LiquidacionRepository extends JpaRepository<Liquidacion, UUID> {
    Optional<Liquidacion> findBySesionJuegoId(UUID sesionJuegoId);
}
