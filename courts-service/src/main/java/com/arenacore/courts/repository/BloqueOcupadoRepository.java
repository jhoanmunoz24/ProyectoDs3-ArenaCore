package com.arenacore.courts.repository;

import com.arenacore.courts.domain.BloqueOcupado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BloqueOcupadoRepository extends JpaRepository<BloqueOcupado, UUID> {
    List<BloqueOcupado> findByFechaAndEstadoIn(LocalDate fecha, List<String> estados);
    List<BloqueOcupado> findByReservaId(UUID reservaId);
    List<BloqueOcupado> findBySesionJuegoId(UUID sesionJuegoId);
    boolean existsByCanchaIdAndFechaAndBloqueHoraAndEstadoIn(UUID canchaId, LocalDate fecha, int bloqueHora, List<String> estados);
}
