package com.resumeiq.resumeiq.controller;

import com.resumeiq.resumeiq.dto.ResumeDtos;
import com.resumeiq.resumeiq.entity.GeneratedResume;
import com.resumeiq.resumeiq.entity.Resume;
import com.resumeiq.resumeiq.entity.User;
import com.resumeiq.resumeiq.service.AnalysisService;
import com.resumeiq.resumeiq.service.AuthService;
import com.resumeiq.resumeiq.service.GeneratedResumeService;
import com.resumeiq.resumeiq.service.ResumeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ResumeController {
    private final ResumeService resumeService;
    private final AuthService authService;
    private final GeneratedResumeService generatedResumeService;
    private final AnalysisService analysisService;

    public ResumeController(ResumeService resumeService,
                           AuthService authService,
                           GeneratedResumeService generatedResumeService,
                           AnalysisService analysisService) {
        this.resumeService = resumeService;
        this.authService = authService;
        this.generatedResumeService = generatedResumeService;
        this.analysisService = analysisService;
    }

    @PostMapping(value = "/resumes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeDtos.ResumeResponse> uploadResume(@AuthenticationPrincipal UserDetails principal,
                                                                 @RequestParam("file") MultipartFile file) {
        User user = authService.getUserByEmail(principal.getUsername());
        Resume resume = resumeService.uploadResume(file, user);
        return ResponseEntity.ok(resumeService.toDto(resume));
    }

    @GetMapping("/resumes")
    public ResponseEntity<List<ResumeDtos.ResumeResponse>> getResumes(@AuthenticationPrincipal UserDetails principal) {
        User user = authService.getUserByEmail(principal.getUsername());
        List<ResumeDtos.ResumeResponse> response = resumeService.getUserResumes(user).stream().map(resumeService::toDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resumes/{id}")
    public ResponseEntity<ResumeDtos.ResumeResponse> getResume(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = authService.getUserByEmail(principal.getUsername());
        Resume resume = resumeService.getResumeById(id, user);
        return ResponseEntity.ok(resumeService.toDto(resume));
    }

    @GetMapping("/resumes/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadResume(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = authService.getUserByEmail(principal.getUsername());
        byte[] data = resumeService.downloadResume(id, user);
        Resume resume = resumeService.getResumeById(id, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getOriginalFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(data));
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Void> deleteResume(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = authService.getUserByEmail(principal.getUsername());
        resumeService.deleteResume(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resumes/{id}/generate")
    public ResponseEntity<com.resumeiq.resumeiq.dto.GeneratedResumeDtos.GeneratedResumeResponse> generate(@AuthenticationPrincipal UserDetails principal,
                                                                                                     @PathVariable Long id,
                                                                                                     @RequestParam(required = false) Long analysisId) {
        User user = authService.getUserByEmail(principal.getUsername());
        Long analysis = analysisId == null ? analysisService.getLatestAnalysisIdForResume(id, user) : analysisId;
        if (analysis == null) {
            throw new IllegalArgumentException("A resume analysis is required before generating an improved resume");
        }
        GeneratedResume generatedResume = generatedResumeService.generateResume(id, analysis, user);
        return ResponseEntity.ok(generatedResumeService.toDto(generatedResume));
    }
}
