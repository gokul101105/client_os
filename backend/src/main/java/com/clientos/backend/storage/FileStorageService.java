package com.clientos.backend.storage;

import com.clientos.backend.exception.InvalidDocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

// Owns all disk I/O for document storage. Kept separate from DocumentService
// on purpose: DocumentService decides *whether* a file may be stored (auth,
// validation), this class only knows *how* bytes get on and off disk. That
// split is also what would make swapping local disk for S3 later a
// change confined to this one class.
@Component
public class FileStorageService {

    private final Path baseDir;

    public FileStorageService(@Value("${app.storage.base-dir}") String baseDir) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create storage directory: " + this.baseDir, e);
        }
    }

    // Stores the file under a server-generated name — the caller-supplied
    // filename is never used to build a path, so there is nothing for a
    // "../../etc/passwd"-style filename to traverse. Returns the path
    // relative to baseDir, which is what gets persisted in the DB.
    public String store(MultipartFile file, Long clientId, String extension) {
        Path clientDir = resolveWithinBase(baseDir, clientId.toString());
        String storedFilename = UUID.randomUUID() + "." + extension;
        Path target = resolveWithinBase(clientDir, storedFilename);

        try {
            Files.createDirectories(clientDir);
            file.transferTo(target);
        } catch (IOException e) {
            throw new InvalidDocumentException("Failed to store file: " + e.getMessage());
        }

        return baseDir.relativize(target).toString();
    }

    public void delete(String relativePath) {
        Path target = resolveWithinBase(baseDir, relativePath);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new InvalidDocumentException("Failed to delete file: " + e.getMessage());
        }
    }

    // Defense in depth: even though callers only ever pass a UUID-generated
    // segment or a numeric client id, re-verify the resolved path can't
    // escape baseDir before touching the filesystem.
    private Path resolveWithinBase(Path parent, String segment) {
        Path resolved = parent.resolve(segment).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new InvalidDocumentException("Invalid storage path");
        }
        return resolved;
    }
}
