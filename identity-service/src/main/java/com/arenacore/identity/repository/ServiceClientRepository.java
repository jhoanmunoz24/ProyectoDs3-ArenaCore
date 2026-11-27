package com.arenacore.identity.repository;

import com.arenacore.identity.domain.ServiceClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceClientRepository extends JpaRepository<ServiceClient, String> {
}
