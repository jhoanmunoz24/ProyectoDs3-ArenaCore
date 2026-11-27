package com.arenacore.courts.repository;

import com.arenacore.courts.domain.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CanchaRepository extends JpaRepository<Cancha, UUID> {
    List<Cancha> findBySedeIdOrderByNombreAsc(UUID sedeId);
}
