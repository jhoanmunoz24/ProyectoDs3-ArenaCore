package com.arenacore.courts.repository;

import com.arenacore.courts.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    List<Reserva> findByFechaOrderByHoraInicioAsc(LocalDate fecha);
}
