package com.clientos.backend.service;

import com.clientos.backend.dto.SubmitClientCreateRequest;
import com.clientos.backend.entity.Client;
import com.clientos.backend.entity.ClientApprovalRequest;
import com.clientos.backend.entity.ClientRequestStatus;
import com.clientos.backend.entity.User;
import com.clientos.backend.exception.ClientNotFoundException;
import com.clientos.backend.exception.ClientRequestNotFoundException;
import com.clientos.backend.exception.InvalidRequestStateException;
import com.clientos.backend.repository.ClientRepository;
import com.clientos.backend.repository.ClientRequestRepository;
import com.clientos.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientRequestService {

    private static final String DEFAULT_PLAN = "FREE";

    private final ClientRequestRepository clientRequestRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public ClientRequestService(
            ClientRequestRepository clientRequestRepository,
            ClientRepository clientRepository,
            UserRepository userRepository
    ) {
        this.clientRequestRepository = clientRequestRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ClientApprovalRequest submitCreateRequest(SubmitClientCreateRequest request, String email) {
        User requestedBy = resolveUser(email);
        ClientApprovalRequest approvalRequest = ClientApprovalRequest.forCreate(
                requestedBy,
                request.name(),
                request.industry(),
                request.plan() != null ? request.plan() : DEFAULT_PLAN
        );
        return clientRequestRepository.save(approvalRequest);
    }

    @Transactional
    public ClientApprovalRequest submitDeleteRequest(Long clientId, String email) {
        User requestedBy = resolveUser(email);
        // Ownership-scoped lookup: an Account Manager can only request
        // deletion of a client they actually own -- same isolation rule
        // used everywhere else a client is looked up by a non-admin caller.
        Client client = clientRepository.findByIdAndOwnerId(clientId, requestedBy.getId())
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        ClientApprovalRequest approvalRequest = ClientApprovalRequest.forDelete(requestedBy, client);
        return clientRequestRepository.save(approvalRequest);
    }

    public List<ClientApprovalRequest> listMine(String email) {
        User requestedBy = resolveUser(email);
        return clientRequestRepository.findAllByRequestedByIdOrderByRequestedAtDesc(requestedBy.getId());
    }

    public List<ClientApprovalRequest> listForAdmin(ClientRequestStatus statusFilter) {
        return statusFilter != null
                ? clientRequestRepository.findAllByStatusOrderByRequestedAtDesc(statusFilter)
                : clientRequestRepository.findAllOrderByRequestedAtDesc();
    }

    @Transactional
    public ClientApprovalRequest approve(Long requestId, String adminEmail, String decisionNote) {
        ClientApprovalRequest request = requirePending(requestId);
        User admin = resolveUser(adminEmail);

        switch (request.getType()) {
            case CREATE -> {
                Client client = new Client(
                        request.getSnapshotName(),
                        request.getRequestedBy(),
                        request.getSnapshotIndustry(),
                        request.getSnapshotPlan() != null ? request.getSnapshotPlan() : DEFAULT_PLAN
                );
                clientRepository.save(client);
            }
            case DELETE -> {
                Client client = request.getTargetClient();
                if (client == null) {
                    throw new InvalidRequestStateException(
                            "Request " + requestId + " targets a client that no longer exists"
                    );
                }
                request.clearTargetClient();
                clientRepository.delete(client);
            }
        }

        request.approve(admin, decisionNote);
        return request;
    }

    @Transactional
    public ClientApprovalRequest reject(Long requestId, String adminEmail, String decisionNote) {
        ClientApprovalRequest request = requirePending(requestId);
        User admin = resolveUser(adminEmail);
        request.reject(admin, decisionNote);
        return request;
    }

    private ClientApprovalRequest requirePending(Long requestId) {
        ClientApprovalRequest request = clientRequestRepository.findWithAssociationsById(requestId)
                .orElseThrow(() -> new ClientRequestNotFoundException(requestId));
        if (request.getStatus() != ClientRequestStatus.PENDING) {
            throw new InvalidRequestStateException(
                    "Request " + requestId + " has already been " + request.getStatus().name().toLowerCase()
            );
        }
        return request;
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
