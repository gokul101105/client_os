package com.clientos.backend.controller;

import com.clientos.backend.dto.AiChatRequest;
import com.clientos.backend.dto.AiChatResponse;
import com.clientos.backend.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients/{clientId}/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    public AiChatResponse chat(
            @PathVariable Long clientId,
            @Valid @RequestBody AiChatRequest request,
            Authentication authentication
    ) {
        return aiAssistantService.chat(clientId, request.message(), authentication.getName());
    }
}
