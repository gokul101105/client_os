package com.clientos.backend.service;

import com.clientos.backend.dto.AiServiceMeetingBriefResponse;
import com.clientos.backend.dto.MeetingBriefResponse;
import com.clientos.backend.entity.Client;
import com.clientos.backend.integration.AiServiceClient;
import org.springframework.stereotype.Service;

@Service
public class MeetingBriefService {

    private final ClientService clientService;
    private final AiServiceClient aiServiceClient;

    public MeetingBriefService(ClientService clientService, AiServiceClient aiServiceClient) {
        this.clientService = clientService;
        this.aiServiceClient = aiServiceClient;
    }

    public MeetingBriefResponse prepare(Long clientId, String email) {
        // Same authorization-before-any-network-call pattern used
        // throughout: if this client isn't the caller's, this throws
        // ClientNotFoundException (404) before the AI service is ever
        // contacted.
        Client client = clientService.findByIdForCurrentUser(clientId, email);

        AiServiceMeetingBriefResponse aiResponse = aiServiceClient.meetingBrief(
                client.getId(),
                client.getName(),
                client.getIndustry(),
                client.getPlan(),
                client.getOpenIssuesCount() != null ? client.getOpenIssuesCount() : 0
        );

        return MeetingBriefResponse.from(aiResponse);
    }
}
