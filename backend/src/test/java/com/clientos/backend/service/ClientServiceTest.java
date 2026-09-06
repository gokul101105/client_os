package com.clientos.backend.service;

import com.clientos.backend.entity.Client;
import com.clientos.backend.entity.User;
import com.clientos.backend.exception.ClientNotFoundException;
import com.clientos.backend.repository.ClientRepository;
import com.clientos.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Module 17: proves the actual authorization decision -- not just an
// HTTP-level symptom of it -- because this is where cross-employee
// access is actually prevented or not. No Spring context, no database:
// a pure unit test of ClientService against mocked repositories.
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClientService clientService;

    private final User employeeA = new User("Employee A", "a@test.com", "hash", "EMPLOYEE");

    @BeforeEach
    void setUp() {
        lenient().when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(employeeA));
    }

    @Test
    void employeeCannotAccessAnotherEmployeesClient() {
        // Client 99 might genuinely exist -- it just doesn't belong to
        // employeeA. The repository query is scoped by owner id, so it
        // returns nothing for her regardless of whether the client
        // exists under a different owner.
        when(clientRepository.findByIdAndOwnerId(eq(99L), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findByIdForCurrentUser(99L, "a@test.com"))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void employeeCanAccessTheirOwnClient() {
        Client ownClient = new Client("Acme Corp", employeeA, "Retail", "Pro");
        when(clientRepository.findByIdAndOwnerId(eq(5L), any())).thenReturn(Optional.of(ownClient));

        Client result = clientService.findByIdForCurrentUser(5L, "a@test.com");

        assertThat(result).isSameAs(ownClient);
    }

    @Test
    void deletingAnotherEmployeesClientIsRejectedBeforeAnyDeleteHappens() {
        when(clientRepository.findByIdAndOwnerId(eq(99L), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.delete(99L, "a@test.com"))
                .isInstanceOf(ClientNotFoundException.class);

        verify(clientRepository, never()).delete(any());
    }
}
