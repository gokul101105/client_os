package com.clientos.backend.service;

import com.clientos.backend.dto.AiServiceSummarizeResponse;
import com.clientos.backend.dto.ClientSummaryResponse;
import com.clientos.backend.entity.Client;
import com.clientos.backend.entity.ClientSummary;
import com.clientos.backend.entity.User;
import com.clientos.backend.exception.ClientSummaryNotFoundException;
import com.clientos.backend.integration.AiServiceClient;
import com.clientos.backend.repository.ClientSummaryRepository;
import com.clientos.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientSummaryService {

    private final ClientService clientService;
    private final ClientSummaryRepository clientSummaryRepository;
    private final UserRepository userRepository;
    private final AiServiceClient aiServiceClient;

    public ClientSummaryService(
            ClientService clientService,
            ClientSummaryRepository clientSummaryRepository,
            UserRepository userRepository,
            AiServiceClient aiServiceClient
    ) {
        this.clientService = clientService;
        this.clientSummaryRepository = clientSummaryRepository;
        this.userRepository = userRepository;
        this.aiServiceClient = aiServiceClient;
    }

    @Transactional
    public ClientSummaryResponse generate(Long clientId, String email) {
        // Same authorization-before-any-network-call pattern as chat
        // (Module 12): if this client isn't the caller's, this throws
        // ClientNotFoundException (404) and the AI service is never
        // contacted.
        Client client = clientService.findByIdForCurrentUser(clientId, email);
        User generatedBy = resolveUser(email);

        AiServiceSummarizeResponse aiResponse = aiServiceClient.summarize(
                client.getId(), client.getName(), client.getIndustry(), client.getPlan()
        );

        ClientSummary summary = new ClientSummary(
                client,
                aiResponse.company(),
                aiResponse.currentSituation(),
                aiResponse.majorProblems(),
                aiResponse.recentActivity(),
                aiResponse.sentiment(),
                aiResponse.attentionRequired(),
                aiResponse.attentionReason(),
                generatedBy
        );
        clientSummaryRepository.save(summary);
        return ClientSummaryResponse.from(summary);
    }

    public ClientSummaryResponse getLatest(Long clientId, String email) {
        Client client = clientService.findByIdForCurrentUser(clientId, email);
        ClientSummary summary = clientSummaryRepository.findLatestByClientId(client.getId())
                .orElseThrow(() -> new ClientSummaryNotFoundException(clientId));
        return ClientSummaryResponse.from(summary);
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
