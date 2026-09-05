package com.clientos.backend.service;

import com.clientos.backend.dto.ClientRequest;
import com.clientos.backend.entity.Client;
import com.clientos.backend.exception.ClientNotFoundException;
import com.clientos.backend.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Transactional
    public Client create(ClientRequest request) {
        Client client = new Client(request.name(), request.ownerId());
        return clientRepository.save(client);
    }

    @Transactional
    public Client update(Long id, ClientRequest request) {
        Client client = findById(id);
        client.setName(request.name());
        client.setOwnerId(request.ownerId());
        return clientRepository.save(client);
    }

    @Transactional
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException(id);
        }
        clientRepository.deleteById(id);
    }
}
