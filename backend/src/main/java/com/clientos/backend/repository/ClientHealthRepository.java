package com.clientos.backend.repository;

import com.clientos.backend.entity.ClientHealth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientHealthRepository extends JpaRepository<ClientHealth, Long> {

    Optional<ClientHealth> findFirstByClientIdOrderByComputedAtDesc(Long clientId);
}
