package com.clientos.backend.repository;

import com.clientos.backend.entity.ClientApprovalRequest;
import com.clientos.backend.entity.ClientRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Every query here eagerly fetches requestedBy/decidedBy/targetClient (the
// latter two with LEFT JOIN since they're nullable -- an inner JOIN would
// silently drop PENDING rows, which never have a decider, and CREATE rows,
// which never have a target client). This project runs with
// spring.jpa.open-in-view=false on purpose, so anything these entities'
// DTOs read must already be loaded before the transactional service method
// returns -- a plain findById() here was the exact bug that shipped first:
// the response DTO tried to read requestedBy/decidedBy after the session
// had already closed and got a LazyInitializationException.
public interface ClientRequestRepository extends JpaRepository<ClientApprovalRequest, Long> {

    @Query("""
            SELECT r FROM ClientApprovalRequest r
            LEFT JOIN FETCH r.requestedBy
            LEFT JOIN FETCH r.decidedBy
            LEFT JOIN FETCH r.targetClient
            WHERE r.id = :id
            """)
    Optional<ClientApprovalRequest> findWithAssociationsById(@Param("id") Long id);

    @Query("""
            SELECT r FROM ClientApprovalRequest r
            LEFT JOIN FETCH r.requestedBy
            LEFT JOIN FETCH r.decidedBy
            LEFT JOIN FETCH r.targetClient
            WHERE r.requestedBy.id = :userId
            ORDER BY r.requestedAt DESC
            """)
    List<ClientApprovalRequest> findAllByRequestedByIdOrderByRequestedAtDesc(@Param("userId") Long userId);

    @Query("""
            SELECT r FROM ClientApprovalRequest r
            LEFT JOIN FETCH r.requestedBy
            LEFT JOIN FETCH r.decidedBy
            LEFT JOIN FETCH r.targetClient
            WHERE r.status = :status
            ORDER BY r.requestedAt DESC
            """)
    List<ClientApprovalRequest> findAllByStatusOrderByRequestedAtDesc(@Param("status") ClientRequestStatus status);

    @Query("""
            SELECT r FROM ClientApprovalRequest r
            LEFT JOIN FETCH r.requestedBy
            LEFT JOIN FETCH r.decidedBy
            LEFT JOIN FETCH r.targetClient
            ORDER BY r.requestedAt DESC
            """)
    List<ClientApprovalRequest> findAllOrderByRequestedAtDesc();
}
