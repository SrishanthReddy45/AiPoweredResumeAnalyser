package com.resumeiq.resumeiq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GeneratedResumeDtos {
    public record GeneratedResumeResponse(Long id, Long resumeId, String filename, String fileType, String content) {}
    public record GeneratedResumePatchRequest(@NotBlank(message = "Resume content is required") @Size(max = 50000, message = "Resume content must be 50,000 characters or fewer") String content) {}
}
