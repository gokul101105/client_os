package com.clientos.backend.dto;

import com.clientos.backend.entity.Document;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String fileName,
        String fileType,
        String uploadedByName,
        LocalDateTime uploadedAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getFileType(),
                document.getUploadedBy().getName(),
                document.getUploadedAt()
        );
    }
}
