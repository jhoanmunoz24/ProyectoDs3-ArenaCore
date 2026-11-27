package com.arenacore.courts.repository;

import com.arenacore.courts.domain.SesionJuego;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SesionJuegoRepository extends JpaRepository<SesionJuego, UUID> {
    List<SesionJuego> findByEstadoInOrderByHoraEntradaAsc(List<String> estados);
}
