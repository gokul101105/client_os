package com.clientos.backend.controller;

import com.clientos.backend.dto.ClientHealthResponse;
import com.clientos.backend.service.ClientHealthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients/{clientId}/health")
public class ClientHealthController {

    private final ClientHealthService clientHealthService;

    public ClientHealthController(ClientHealthService clientHealthService) {
        this.clientHealthService = clientHealthService;
    }

    @GetMapping
    public ClientHealthResponse getLatest(@PathVariable Long clientId, Authentication authentication) {
        return clientHealthService.getLatest(clientId, authentication.getName());
    }

    @PostMapping("/recompute")
    public ClientHealthResponse recompute(@PathVariable Long clientId, Authentication authentication) {
        return clientHealthService.recompute(clientId, authentication.getName());
    }
}
