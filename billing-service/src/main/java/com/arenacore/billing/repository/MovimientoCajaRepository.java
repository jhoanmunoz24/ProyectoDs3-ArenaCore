package com.arenacore.billing.repository;

import com.arenacore.billing.domain.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, UUID> {
    List<MovimientoCaja> findBySesionCajaIdOrderByCreadoEnAsc(UUID sesionCajaId);
}
