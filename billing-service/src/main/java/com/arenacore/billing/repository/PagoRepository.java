package com.arenacore.billing.repository;

import com.arenacore.billing.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PagoRepository extends JpaRepository<Pago, UUID> {
    List<Pago> findByLiquidacionId(UUID liquidacionId);
}
