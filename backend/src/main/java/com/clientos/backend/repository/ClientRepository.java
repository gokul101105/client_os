package com.clientos.backend.repository;

import com.clientos.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT c FROM Client c JOIN FETCH c.owner WHERE c.owner.id = :ownerId")
    List<Client> findAllByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT c FROM Client c JOIN FETCH c.owner WHERE c.id = :id AND c.owner.id = :ownerId")
    Optional<Client> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
