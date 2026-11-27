package com.arenacore.billing.repository;

import com.arenacore.billing.domain.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ComprobanteRepository extends JpaRepository<Comprobante, UUID> {
    Optional<Comprobante> findByLiquidacionId(UUID liquidacionId);
    Optional<Comprobante> findByPagoId(UUID pagoId);
}
