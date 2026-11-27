package com.arenacore.equipment.repository;

import com.arenacore.equipment.domain.Implemento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImplementoRepository extends JpaRepository<Implemento, UUID> {
    List<Implemento> findByActivoTrueAndStockDisponibleGreaterThanOrderByNombreAsc(int minStock);
    boolean existsByCodigoIgnoreCase(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Implemento i WHERE i.id = :id")
    Optional<Implemento> findByIdForUpdate(UUID id);
}
