package com.resumeiq.resumeiq.controller;

import com.resumeiq.resumeiq.dto.AnalysisDtos;
import com.resumeiq.resumeiq.entity.ResumeAnalysis;
import com.resumeiq.resumeiq.entity.User;
import com.resumeiq.resumeiq.service.AnalysisService;
import com.resumeiq.resumeiq.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {
    private final AnalysisService analysisService;
    private final AuthService authService;

    public AnalysisController(AnalysisService analysisService, AuthService authService) {
        this.analysisService = analysisService;
        this.authService = authService;
    }

    @PostMapping("/analyses/general")
    public ResponseEntity<AnalysisDtos.AnalysisResponse> general(@AuthenticationPrincipal UserDetails principal,
                                                                @RequestParam Long resumeId,
                                                                @Valid @RequestBody(required = false) AnalysisDtos.AnalyzeGeneralRequest request) {
        User user = authService.getUserByEmail(principal.getUsername());
        String jobDescription = request == null ? "" : request.jobDescription();
        ResumeAnalysis analysis = analysisService.analyzeGeneral(resumeId, user, jobDescription);
        return ResponseEntity.ok(analysisService.toDto(analysis));
    }

    @PostMapping("/analyses/job-match")
    public ResponseEntity<AnalysisDtos.AnalysisResponse> jobMatch(@AuthenticationPrincipal UserDetails principal,
                                                                @RequestParam Long resumeId,
                                                                @Valid @RequestBody AnalysisDtos.AnalyzeGeneralRequest request) {
        User user = authService.getUserByEmail(principal.getUsername());
        ResumeAnalysis analysis = analysisService.analyzeGeneral(resumeId, user, request.jobDescription());
        return ResponseEntity.ok(analysisService.toDto(analysis));
    }

    @GetMapping("/analyses")
    public ResponseEntity<List<AnalysisDtos.AnalysisResponse>> all(@AuthenticationPrincipal UserDetails principal) {
        User user = authService.getUserByEmail(principal.getUsername());
        return ResponseEntity.ok(analysisService.getAnalysesForUser(user).stream().map(analysisService::toDto).toList());
    }

    @GetMapping("/analyses/{id}")
    public ResponseEntity<AnalysisDtos.AnalysisResponse> getById(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        User user = authService.getUserByEmail(principal.getUsername());
        return ResponseEntity.ok(analysisService.toDto(analysisService.getAnalysis(id, user)));
    }

    @GetMapping("/analyses/{id}/report")
    public ResponseEntity<AnalysisDtos.AnalysisResponse> report(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        return getById(principal, id);
    }
}
