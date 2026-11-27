package com.arenacore.equipment.repository;

import com.arenacore.equipment.domain.CategoriaImplemento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoriaImplementoRepository extends JpaRepository<CategoriaImplemento, UUID> {
    Optional<CategoriaImplemento> findByCodigoIgnoreCase(String codigo);
}
