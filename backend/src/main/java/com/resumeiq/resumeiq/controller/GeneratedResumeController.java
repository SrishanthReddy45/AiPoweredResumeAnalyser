package com.resumeiq.resumeiq.controller;

import com.resumeiq.resumeiq.dto.GeneratedResumeDtos;
import com.resumeiq.resumeiq.entity.GeneratedResume;
import com.resumeiq.resumeiq.entity.User;
import com.resumeiq.resumeiq.service.AuthService;
import com.resumeiq.resumeiq.service.GeneratedResumeService;
import org.springframework.core.io.ByteArrayResource;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GeneratedResumeController {
    private final GeneratedResumeService generatedResumeService;
    private final AuthService authService;

    public GeneratedResumeController(GeneratedResumeService generatedResumeService, AuthService authService) {
        this.generatedResumeService = generatedResumeService;
        this.authService = authService;
    }

    @GetMapping("/generated-resumes")
    public ResponseEntity<List<GeneratedResumeDtos.GeneratedResumeResponse>> list(@AuthenticationPrincipal UserDetails principal) {
        User user = authService.getUserByEmail(principal.getUsername());
        return ResponseEntity.ok(generatedResumeService.getGeneratedResumes(user).stream().map(generatedResumeService::toDto).toList());
    }

    @GetMapping("/generated-resumes/{id}")
    public ResponseEntity<GeneratedResumeDtos.GeneratedResumeResponse> getById(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = authService.getUserByEmail(principal.getUsername());
        return ResponseEntity.ok(generatedResumeService.toDto(generatedResumeService.getGeneratedResume(id, user)));
    }

    @PatchMapping("/generated-resumes/{id}")
    public ResponseEntity<GeneratedResumeDtos.GeneratedResumeResponse> patch(@AuthenticationPrincipal UserDetails principal,
                                                                          @PathVariable Long id,
                                                                          @Valid @RequestBody GeneratedResumeDtos.GeneratedResumePatchRequest request) {
        User user = authService.getUserByEmail(principal.getUsername());
        GeneratedResume generatedResume = generatedResumeService.patchGeneratedResume(id, user, request.content());
        return ResponseEntity.ok(generatedResumeService.toDto(generatedResume));
    }

    @GetMapping("/generated-resumes/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@AuthenticationPrincipal UserDetails principal,
                                                    @PathVariable Long id,
                                                    @RequestParam(defaultValue = "docx") String format) {
        User user = authService.getUserByEmail(principal.getUsername());
        byte[] data = generatedResumeService.downloadGeneratedResume(id, user, format);
        String safeFormat = format.toLowerCase(java.util.Locale.ROOT);
        MediaType contentType = "pdf".equals(safeFormat)
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"improved-resume." + safeFormat + "\"")
                .contentType(contentType)
                .body(new ByteArrayResource(data));
    }
}
