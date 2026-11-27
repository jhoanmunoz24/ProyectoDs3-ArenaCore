package com.arenacore.billing.repository;

import com.arenacore.billing.domain.SesionCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SesionCajaRepository extends JpaRepository<SesionCaja, UUID> {
    Optional<SesionCaja> findByCajaIdAndEstado(UUID cajaId, String estado);
    Optional<SesionCaja> findFirstByOperadorIdAndEstadoOrderByAbiertaEnDesc(UUID operadorId, String estado);
    Optional<SesionCaja> findFirstByEstadoOrderByAbiertaEnDesc(String estado);
}
