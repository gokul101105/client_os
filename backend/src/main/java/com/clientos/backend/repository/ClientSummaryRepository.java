package com.clientos.backend.repository;

import com.clientos.backend.entity.ClientSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientSummaryRepository extends JpaRepository<ClientSummary, Long> {

    Optional<ClientSummary> findFirstByClientIdOrderByGeneratedAtDesc(Long clientId);
}
