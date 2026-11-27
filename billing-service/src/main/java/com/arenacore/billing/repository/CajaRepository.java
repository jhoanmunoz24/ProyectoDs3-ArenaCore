package com.arenacore.billing.repository;

import com.arenacore.billing.domain.Caja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CajaRepository extends JpaRepository<Caja, UUID> {
    Optional<Caja> findByCodigo(String codigo);
}
