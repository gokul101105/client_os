package com.clientos.backend.controller;

import com.clientos.backend.dto.AiChatRequest;
import com.clientos.backend.dto.AiChatResponse;
import com.clientos.backend.dto.ClientSummaryResponse;
import com.clientos.backend.service.AiAssistantService;
import com.clientos.backend.service.ClientSummaryService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients/{clientId}/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final ClientSummaryService clientSummaryService;

    public AiAssistantController(AiAssistantService aiAssistantService, ClientSummaryService clientSummaryService) {
        this.aiAssistantService = aiAssistantService;
        this.clientSummaryService = clientSummaryService;
    }

    @PostMapping("/chat")
    public AiChatResponse chat(
            @PathVariable Long clientId,
            @Valid @RequestBody AiChatRequest request,
            Authentication authentication
    ) {
        return aiAssistantService.chat(clientId, request.message(), authentication.getName());
    }

    @PostMapping("/summary")
    public ClientSummaryResponse generateSummary(@PathVariable Long clientId, Authentication authentication) {
        return clientSummaryService.generate(clientId, authentication.getName());
    }

    @GetMapping("/summary")
    public ClientSummaryResponse getLatestSummary(@PathVariable Long clientId, Authentication authentication) {
        return clientSummaryService.getLatest(clientId, authentication.getName());
    }
}
