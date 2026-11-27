package com.arenacore.courts.repository;

import com.arenacore.courts.domain.EventoSesionJuego;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoSesionJuegoRepository extends JpaRepository<EventoSesionJuego, UUID> {
    List<EventoSesionJuego> findBySesionJuegoIdOrderByCreadoEnAsc(UUID sesionJuegoId);
}
