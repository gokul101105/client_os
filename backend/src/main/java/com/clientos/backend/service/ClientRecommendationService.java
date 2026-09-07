package com.clientos.backend.service;

import com.clientos.backend.dto.AiServiceRecommendResponse;
import com.clientos.backend.dto.ClientRecommendationsResponse;
import com.clientos.backend.dto.RecommendationItem;
import com.clientos.backend.entity.Client;
import com.clientos.backend.entity.ClientHealth;
import com.clientos.backend.entity.ClientSummary;
import com.clientos.backend.health.HealthBand;
import com.clientos.backend.integration.AiServiceClient;
import com.clientos.backend.repository.ClientHealthRepository;
import com.clientos.backend.repository.ClientSummaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientRecommendationService {

    private final ClientService clientService;
    private final ClientHealthRepository clientHealthRepository;
    private final ClientSummaryRepository clientSummaryRepository;
    private final AiServiceClient aiServiceClient;

    public ClientRecommendationService(
            ClientService clientService,
            ClientHealthRepository clientHealthRepository,
            ClientSummaryRepository clientSummaryRepository,
            AiServiceClient aiServiceClient
    ) {
        this.clientService = clientService;
        this.clientHealthRepository = clientHealthRepository;
        this.clientSummaryRepository = clientSummaryRepository;
        this.aiServiceClient = aiServiceClient;
    }

    public ClientRecommendationsResponse recommend(Long clientId, String email) {
        // Same authorization-before-any-network-call pattern used
        // throughout: if this client isn't the caller's, this throws
        // ClientNotFoundException (404) before anything else runs.
        Client client = clientService.findByIdForCurrentUser(clientId, email);

        Optional<ClientHealth> health =
                clientHealthRepository.findFirstByClientIdOrderByComputedAtDesc(client.getId());
        Optional<ClientSummary> summary =
                clientSummaryRepository.findLatestByClientId(client.getId());

        List<String> breakdownReasons = health
                .map(h -> h.getBreakdown().stream()
                        .map(item -> item.signal() + ": " + (item.points() > 0 ? "+" : "") + item.points()
                                + " (" + item.reason() + ")")
                        .toList())
                .orElse(List.of());

        AiServiceRecommendResponse aiResponse = aiServiceClient.recommend(
                client.getId(),
                client.getName(),
                client.getIndustry(),
                client.getPlan(),
                health.map(ClientHealth::getScore).orElse(null),
                health.map(h -> HealthBand.valueOf(h.getBand()).getLabel()).orElse(null),
                breakdownReasons,
                summary.map(ClientSummary::getCurrentSituation).orElse(null),
                summary.map(ClientSummary::getMajorProblems).orElse(List.of()),
                summary.map(ClientSummary::getSentiment).orElse(null),
                summary.map(ClientSummary::getAttentionRequired).orElse(null),
                summary.map(ClientSummary::getAttentionReason).orElse(null)
        );

        List<RecommendationItem> items = aiResponse.recommendations().stream()
                .map(item -> new RecommendationItem(item.action(), item.reason(), item.priority()))
                .toList();

        return new ClientRecommendationsResponse(items);
    }
}
