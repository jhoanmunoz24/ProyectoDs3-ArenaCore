package com.arenacore.identity.repository;

import com.arenacore.identity.domain.AutorizacionExcepcional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutorizacionExcepcionalRepository extends JpaRepository<AutorizacionExcepcional, UUID> {
    List<AutorizacionExcepcional> findByEstadoOrderByCreadoEnDesc(String estado);
}
