package com.arenacore.courts.repository;

import com.arenacore.courts.domain.Sede;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SedeRepository extends JpaRepository<Sede, UUID> {
}
