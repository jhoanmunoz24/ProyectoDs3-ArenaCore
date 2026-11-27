package com.arenacore.identity.repository;

import com.arenacore.identity.domain.AuditoriaSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditoriaSeguridadRepository extends JpaRepository<AuditoriaSeguridad, UUID> {
}
