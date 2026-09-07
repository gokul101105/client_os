package com.clientos.backend.service;

import com.clientos.backend.entity.Client;
import com.clientos.backend.entity.Document;
import com.clientos.backend.entity.User;
import com.clientos.backend.exception.DocumentNotFoundException;
import com.clientos.backend.exception.InvalidDocumentException;
import com.clientos.backend.integration.AiServiceClient;
import com.clientos.backend.repository.DocumentRepository;
import com.clientos.backend.repository.UserRepository;
import com.clientos.backend.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    // Extension -> the file_type label stored in the DB. Deliberately an
    // extension allowlist rather than trusting the browser-supplied
    // Content-Type, which is easy to spoof and inconsistent across browsers
    // for DOCX in particular.
    private static final Map<String, String> ALLOWED_EXTENSIONS = Map.of(
            "pdf", "PDF",
            "txt", "TXT",
            "docx", "DOCX"
    );
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final DocumentRepository documentRepository;
    private final ClientService clientService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AiServiceClient aiServiceClient;

    public DocumentService(
            DocumentRepository documentRepository,
            ClientService clientService,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            AiServiceClient aiServiceClient
    ) {
        this.documentRepository = documentRepository;
        this.clientService = clientService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.aiServiceClient = aiServiceClient;
    }

    public List<Document> listForClient(Long clientId, String email) {
        // Throws ClientNotFoundException (404) if this client isn't the
        // caller's — the same ownership check Module 5 already enforces.
        Client client = clientService.findByIdForCurrentUser(clientId, email);
        return documentRepository.findAllByClientIdOrderByUploadedAtDesc(client.getId());
    }

    @Transactional
    public Document upload(Long clientId, MultipartFile file, String email) {
        Client client = clientService.findByIdForCurrentUser(clientId, email);
        User uploader = resolveUser(email);

        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("File must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidDocumentException("File exceeds the 10MB size limit");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : ""
        );
        String extension = extractExtension(originalName);
        String fileType = ALLOWED_EXTENSIONS.get(extension);
        if (fileType == null) {
            throw new InvalidDocumentException("Unsupported file type. Allowed: PDF, TXT, DOCX");
        }

        String storedPath = fileStorageService.store(file, client.getId(), extension);
        Document document = new Document(client, originalName, fileType, storedPath, uploader);
        return documentRepository.save(document);
    }

    // Deliberately NOT called from inside upload()'s own transaction, and
    // not @Transactional itself. The ai-service is a separate process with
    // its own DB connection -- if it were called while upload()'s insert
    // is still uncommitted, its own document_chunks insert would fail a
    // foreign-key check against a document row it can't see yet (this
    // shipped once and was caught live: "document_id=999 is not present in
    // table documents", except with a real, just-inserted id instead of a
    // fake one). Calling this only after upload() has already returned --
    // see DocumentController.upload() -- guarantees the row is committed
    // and visible first. This method's own DB access (the read here, the
    // delete on failure) each get Spring Data's normal per-call
    // transaction, which is all either needs.
    public void processForAi(Long clientId, Long documentId, String email) {
        Client client = clientService.findByIdForCurrentUser(clientId, email);
        Document document = documentRepository.findByIdAndClientId(documentId, client.getId())
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        try {
            aiServiceClient.processDocument(clientId, documentId, document.getFilePath());
        } catch (RuntimeException e) {
            // An unprocessable document is useless to every AI feature, so
            // don't leave it behind -- undo the upload entirely rather than
            // silently storing a permanently-inert document.
            delete(clientId, documentId, email);
            throw e;
        }
    }

    @Transactional
    public void delete(Long clientId, Long documentId, String email) {
        Client client = clientService.findByIdForCurrentUser(clientId, email);
        Document document = documentRepository.findByIdAndClientId(documentId, client.getId())
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        fileStorageService.delete(document.getFilePath());
        documentRepository.delete(document);
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
