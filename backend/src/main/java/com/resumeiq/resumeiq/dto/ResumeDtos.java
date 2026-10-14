package com.resumeiq.resumeiq.dto;

import java.time.Instant;

public class ResumeDtos {
    public record ResumeResponse(Long id, String originalFilename, String fileType, Long fileSize, String processingStatus,
                                String extractedText, String parsedResumeData, Instant createdAt) {}

    public record ResumeUploadSummary(Long id, String filename, String status, String message) {}
}
