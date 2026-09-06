package com.clientos.backend.service;

import com.clientos.backend.dto.AiChatResponse;
import com.clientos.backend.entity.Client;
import com.clientos.backend.integration.AiServiceClient;
import org.springframework.stereotype.Service;

@Service
public class AiAssistantService {

    private final ClientService clientService;
    private final AiServiceClient aiServiceClient;

    public AiAssistantService(ClientService clientService, AiServiceClient aiServiceClient) {
        this.clientService = clientService;
        this.aiServiceClient = aiServiceClient;
    }

    public AiChatResponse chat(Long clientId, String message, String email) {
        // Authorization happens here, before any network call leaves this
        // process: if clientId isn't the caller's, this throws
        // ClientNotFoundException (404) and the AI service is never
        // contacted at all. Employee A can never even trigger a lookup
        // against Employee B's client — there is no code path where the
        // request reaches the AI service without first passing this check.
        Client client = clientService.findByIdForCurrentUser(clientId, email);
        return AiChatResponse.from(aiServiceClient.chat(client.getId(), message));
    }
}
