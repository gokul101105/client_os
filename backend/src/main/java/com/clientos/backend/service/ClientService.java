package com.clientos.backend.service;

import com.clientos.backend.dto.ClientRequest;
import com.clientos.backend.entity.Client;
import com.clientos.backend.entity.User;
import com.clientos.backend.exception.ClientNotFoundException;
import com.clientos.backend.repository.ClientRepository;
import com.clientos.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientService {

    private static final String DEFAULT_PLAN = "FREE";

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public ClientService(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    public List<Client> findAllForCurrentUser(String email) {
        User owner = resolveUser(email);
        return clientRepository.findAllByOwnerId(owner.getId());
    }

    public Client findByIdForCurrentUser(Long id, String email) {
        User owner = resolveUser(email);
        return clientRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Transactional
    public Client update(Long id, ClientRequest request, String email) {
        Client client = findByIdForCurrentUser(id, email);
        client.setName(request.name());
        client.setIndustry(request.industry());
        client.setPlan(request.plan() != null ? request.plan() : DEFAULT_PLAN);
        return clientRepository.save(client);
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
