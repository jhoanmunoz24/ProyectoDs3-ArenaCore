package com.arenacore.billing.repository;

import com.arenacore.billing.domain.TarifaCancha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarifaCanchaRepository extends JpaRepository<TarifaCancha, UUID> {
    Optional<TarifaCancha> findByTipoCanchaAndFranjaAndActivaTrue(String tipoCancha, String franja);
    List<TarifaCancha> findByActivaTrueOrderByTipoCanchaAscFranjaAsc();
}
