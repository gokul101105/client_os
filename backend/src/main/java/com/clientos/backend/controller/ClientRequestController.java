package com.clientos.backend.controller;

import com.clientos.backend.dto.ClientApprovalRequestResponse;
import com.clientos.backend.dto.SubmitClientCreateRequest;
import com.clientos.backend.service.ClientRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientRequestController {

    private final ClientRequestService clientRequestService;

    public ClientRequestController(ClientRequestService clientRequestService) {
        this.clientRequestService = clientRequestService;
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientApprovalRequestResponse submitCreateRequest(
            @Valid @RequestBody SubmitClientCreateRequest request,
            Authentication authentication
    ) {
        return ClientApprovalRequestResponse.from(
                clientRequestService.submitCreateRequest(request, authentication.getName())
        );
    }

    @PostMapping("/{id}/delete-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientApprovalRequestResponse submitDeleteRequest(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ClientApprovalRequestResponse.from(
                clientRequestService.submitDeleteRequest(id, authentication.getName())
        );
    }

    @GetMapping("/requests/mine")
    public List<ClientApprovalRequestResponse> mine(Authentication authentication) {
        return clientRequestService.listMine(authentication.getName())
                .stream().map(ClientApprovalRequestResponse::from).toList();
    }
}
