package com.clientos.backend.service;

import com.clientos.backend.entity.Role;
import com.clientos.backend.entity.User;
import com.clientos.backend.exception.ClientNotFoundException;
import com.clientos.backend.repository.ClientRepository;
import com.clientos.backend.repository.ClientRequestRepository;
import com.clientos.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Relocated from the old ClientServiceTest.deletingAnotherEmployeesClientIsRejected...
// test, which proved a direct ClientService.delete() call could not touch
// another Account Manager's client. That method no longer exists -- deletion
// is only reachable through a delete-request, so the same ownership proof
// now lives here, against ClientRequestService.submitDeleteRequest instead.
@ExtendWith(MockitoExtension.class)
class ClientRequestServiceTest {

    @Mock
    private ClientRequestRepository clientRequestRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClientRequestService clientRequestService;

    private final User accountManagerA = new User("Account Manager A", "a@test.com", "hash", Role.ACCOUNT_MANAGER);

    @BeforeEach
    void setUp() {
        lenient().when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(accountManagerA));
    }

    @Test
    void deleteRequestForAnotherAccountManagersClientIsRejectedBeforeAnyRequestIsCreated() {
        when(clientRepository.findByIdAndOwnerId(eq(99L), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientRequestService.submitDeleteRequest(99L, "a@test.com"))
                .isInstanceOf(ClientNotFoundException.class);

        verify(clientRequestRepository, never()).save(any());
    }
}
