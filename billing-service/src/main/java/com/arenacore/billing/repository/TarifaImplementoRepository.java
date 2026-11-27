package com.arenacore.billing.repository;

import com.arenacore.billing.domain.TarifaImplemento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarifaImplementoRepository extends JpaRepository<TarifaImplemento, UUID> {
    Optional<TarifaImplemento> findByCodigoTarifaAndActivaTrue(String codigoTarifa);
    List<TarifaImplemento> findByActivaTrueOrderByCodigoTarifaAsc();
}
