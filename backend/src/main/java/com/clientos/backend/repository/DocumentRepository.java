package com.clientos.backend.repository;

import com.clientos.backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d JOIN FETCH d.uploadedBy WHERE d.client.id = :clientId ORDER BY d.uploadedAt DESC")
    List<Document> findAllByClientIdOrderByUploadedAtDesc(@Param("clientId") Long clientId);

    // Scoped by clientId (not just the document's own id) so a document can
    // only be fetched/deleted through the client it actually belongs to —
    // the caller is expected to have already verified that client is theirs.
    @Query("SELECT d FROM Document d WHERE d.id = :id AND d.client.id = :clientId")
    Optional<Document> findByIdAndClientId(@Param("id") Long id, @Param("clientId") Long clientId);
}
