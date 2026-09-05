package com.clientos.backend.controller;

import com.clientos.backend.dto.ClientRequest;
import com.clientos.backend.dto.ClientResponse;
import com.clientos.backend.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientResponse> getAll(Authentication authentication) {
        return clientService.findAllForCurrentUser(authentication.getName())
                .stream().map(ClientResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClientResponse getById(@PathVariable Long id, Authentication authentication) {
        return ClientResponse.from(clientService.findByIdForCurrentUser(id, authentication.getName()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@Valid @RequestBody ClientRequest request, Authentication authentication) {
        return ClientResponse.from(clientService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ClientResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request,
            Authentication authentication
    ) {
        return ClientResponse.from(clientService.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        clientService.delete(id, authentication.getName());
    }
}
