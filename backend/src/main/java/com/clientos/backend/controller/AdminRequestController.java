package com.clientos.backend.controller;

import com.clientos.backend.dto.ClientApprovalRequestResponse;
import com.clientos.backend.dto.DecisionRequest;
import com.clientos.backend.entity.ClientRequestStatus;
import com.clientos.backend.service.ClientRequestService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminRequestController {

    private final ClientRequestService clientRequestService;

    public AdminRequestController(ClientRequestService clientRequestService) {
        this.clientRequestService = clientRequestService;
    }

    @GetMapping
    public List<ClientApprovalRequestResponse> list(
            @RequestParam(required = false) ClientRequestStatus status
    ) {
        return clientRequestService.listForAdmin(status)
                .stream().map(ClientApprovalRequestResponse::from).toList();
    }

    @PostMapping("/{id}/approve")
    public ClientApprovalRequestResponse approve(
            @PathVariable Long id,
            @RequestBody(required = false) DecisionRequest request,
            Authentication authentication
    ) {
        String note = request != null ? request.decisionNote() : null;
        return ClientApprovalRequestResponse.from(
                clientRequestService.approve(id, authentication.getName(), note)
        );
    }

    @PostMapping("/{id}/reject")
    public ClientApprovalRequestResponse reject(
            @PathVariable Long id,
            @RequestBody(required = false) DecisionRequest request,
            Authentication authentication
    ) {
        String note = request != null ? request.decisionNote() : null;
        return ClientApprovalRequestResponse.from(
                clientRequestService.reject(id, authentication.getName(), note)
        );
    }
}
