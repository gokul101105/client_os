package com.clientos.backend.service;

import com.clientos.backend.entity.Client;
import com.clientos.backend.entity.Document;
import com.clientos.backend.entity.Role;
import com.clientos.backend.entity.User;
import com.clientos.backend.exception.AiServiceException;
import com.clientos.backend.exception.InvalidDocumentException;
import com.clientos.backend.integration.AiServiceClient;
import com.clientos.backend.repository.DocumentRepository;
import com.clientos.backend.repository.UserRepository;
import com.clientos.backend.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Module 17: file upload validation -- type allowlist and size limit.
// Mocks every collaborator (no database, no real disk I/O) so these run
// as pure, fast unit tests of DocumentService's own validation logic.
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AiServiceClient aiServiceClient;

    private DocumentService documentService;

    private final User uploader = new User("Employee A", "a@test.com", "hash", Role.ACCOUNT_MANAGER);
    private Client client;

    @BeforeEach
    void setUp() {
        documentService =
                new DocumentService(documentRepository, clientService, userRepository, fileStorageService, aiServiceClient);
        client = new Client("Acme Corp", uploader, "Retail", "Pro");
        lenient().when(clientService.findByIdForCurrentUser(1L, "a@test.com")).thenReturn(client);
        lenient().when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(uploader));
    }

    @Test
    void rejectsDisallowedFileType() {
        MockMultipartFile file =
                new MockMultipartFile("file", "malware.exe", "application/octet-stream", "content".getBytes());

        assertThatThrownBy(() -> documentService.upload(1L, file, "a@test.com"))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessageContaining("Unsupported file type");

        // Rejected before it ever touches disk or the database.
        verifyNoInteractions(fileStorageService);
        verifyNoInteractions(documentRepository);
    }

    @Test
    void rejectsOversizedFile() {
        byte[] oversized = new byte[11 * 1024 * 1024]; // 11MB, over the 10MB limit
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", oversized);

        assertThatThrownBy(() -> documentService.upload(1L, file, "a@test.com"))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessageContaining("10MB");

        verifyNoInteractions(fileStorageService);
        verifyNoInteractions(documentRepository);
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> documentService.upload(1L, file, "a@test.com"))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void acceptsValidPdfWithinSizeLimit() {
        MockMultipartFile file = new MockMultipartFile("file", "notes.pdf", "application/pdf", "content".getBytes());
        when(fileStorageService.store(any(), any(), anyString())).thenReturn("1/generated-name.pdf");
        when(documentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Document result = documentService.upload(1L, file, "a@test.com");

        assertThat(result.getFileType()).isEqualTo("PDF");
        assertThat(result.getFileName()).isEqualTo("notes.pdf");
    }

    @Test
    void processForAiDeletesTheDocumentWhenProcessingFails() {
        Document document = new Document(client, "notes.pdf", "PDF", "1/generated-name.pdf", uploader);
        when(documentRepository.findByIdAndClientId(nullable(Long.class), nullable(Long.class)))
                .thenReturn(Optional.of(document));
        doThrow(new AiServiceException("unreachable"))
                .when(aiServiceClient).processDocument(nullable(Long.class), nullable(Long.class), anyString());

        assertThatThrownBy(() -> documentService.processForAi(1L, 1L, "a@test.com"))
                .isInstanceOf(AiServiceException.class);

        // An unprocessable document is useless to every AI feature, so the
        // failed upload is undone entirely rather than left behind inert --
        // see the comment in DocumentService.processForAi().
        verify(fileStorageService).delete("1/generated-name.pdf");
        verify(documentRepository).delete(document);
    }

    @Test
    void filenameExtensionIsCaseInsensitive() {
        MockMultipartFile file = new MockMultipartFile("file", "REPORT.PDF", "application/pdf", "content".getBytes());
        when(fileStorageService.store(any(), any(), anyString())).thenReturn("1/generated-name.pdf");
        when(documentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Document result = documentService.upload(1L, file, "a@test.com");

        assertThat(result.getFileType()).isEqualTo("PDF");
    }
}
