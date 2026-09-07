package com.clientos.backend.service;

import com.clientos.backend.dto.ClientHealthResponse;
import com.clientos.backend.entity.Client;
import com.clientos.backend.entity.ClientHealth;
import com.clientos.backend.entity.ClientSummary;
import com.clientos.backend.exception.ClientHealthNotFoundException;
import com.clientos.backend.health.HealthScoreCalculator;
import com.clientos.backend.health.HealthScoreResult;
import com.clientos.backend.repository.ClientHealthRepository;
import com.clientos.backend.repository.ClientRepository;
import com.clientos.backend.repository.ClientSummaryRepository;
import com.clientos.backend.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ClientHealthService {

    private final ClientService clientService;
    private final ClientRepository clientRepository;
    private final DocumentRepository documentRepository;
    private final ClientSummaryRepository clientSummaryRepository;
    private final ClientHealthRepository clientHealthRepository;
    private final HealthScoreCalculator calculator;

    public ClientHealthService(
            ClientService clientService,
            ClientRepository clientRepository,
            DocumentRepository documentRepository,
            ClientSummaryRepository clientSummaryRepository,
            ClientHealthRepository clientHealthRepository,
            HealthScoreCalculator calculator
    ) {
        this.clientService = clientService;
        this.clientRepository = clientRepository;
        this.documentRepository = documentRepository;
        this.clientSummaryRepository = clientSummaryRepository;
        this.clientHealthRepository = clientHealthRepository;
        this.calculator = calculator;
    }

    @Transactional
    public ClientHealthResponse recompute(Long clientId, String email) {
        Client client = clientService.findByIdForCurrentUser(clientId, email);

        LocalDateTime lastActivity = documentRepository.findMaxUploadedAtByClientId(client.getId());
        Optional<ClientSummary> latestSummary =
                clientSummaryRepository.findLatestByClientId(client.getId());

        HealthScoreResult result = calculator.calculate(
                client.getOpenIssuesCount(),
                lastActivity,
                latestSummary.map(ClientSummary::getSentiment).orElse(null),
                latestSummary.map(ClientSummary::getAttentionRequired).orElse(null)
        );

        ClientHealth health = new ClientHealth(client, result.score(), result.band().name(), result.breakdown());
        clientHealthRepository.save(health);

        // Denormalized onto clients.health_score so the dashboard's
        // existing GET /api/clients (Module 5, unmodified) keeps showing
        // an accurate score without an extra join per card.
        client.setHealthScore(result.score());
        clientRepository.save(client);

        return ClientHealthResponse.from(health);
    }

    public ClientHealthResponse getLatest(Long clientId, String email) {
        Client client = clientService.findByIdForCurrentUser(clientId, email);
        ClientHealth health = clientHealthRepository.findFirstByClientIdOrderByComputedAtDesc(client.getId())
                .orElseThrow(() -> new ClientHealthNotFoundException(clientId));
        return ClientHealthResponse.from(health);
    }
}
