package com.clientos.backend.repository;

import com.clientos.backend.entity.ClientSummary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientSummaryRepository extends JpaRepository<ClientSummary, Long> {

    // JOIN FETCH generatedBy -- and Pageable(0,1) instead of a derived
    // findFirstBy... query -- because this app runs with
    // spring.jpa.open-in-view=false and getLatest() isn't @Transactional:
    // a plain findFirstByClientIdOrderByGeneratedAtDesc() would return a
    // ClientSummary whose generatedBy is still an uninitialized lazy proxy
    // once the (already-closed) per-call transaction ends, and
    // ClientSummaryResponse.from() reading generatedBy.getName() would
    // throw LazyInitializationException. Caught live: a summary that
    // existed from before this session 500'd on every GET.
    @Query("SELECT s FROM ClientSummary s JOIN FETCH s.generatedBy WHERE s.client.id = :clientId ORDER BY s.generatedAt DESC")
    List<ClientSummary> findAllByClientIdOrderByGeneratedAtDesc(@Param("clientId") Long clientId, Pageable pageable);

    default Optional<ClientSummary> findLatestByClientId(Long clientId) {
        List<ClientSummary> results = findAllByClientIdOrderByGeneratedAtDesc(clientId, PageRequest.of(0, 1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
