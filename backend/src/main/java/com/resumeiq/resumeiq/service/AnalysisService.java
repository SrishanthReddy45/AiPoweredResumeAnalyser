package com.resumeiq.resumeiq.service;

import com.resumeiq.resumeiq.ai.AiAnalysisProvider;
import com.resumeiq.resumeiq.dto.AnalysisDtos;
import com.resumeiq.resumeiq.entity.Resume;
import com.resumeiq.resumeiq.entity.ResumeAnalysis;
import com.resumeiq.resumeiq.entity.User;
import com.resumeiq.resumeiq.exception.BadRequestException;
import com.resumeiq.resumeiq.exception.ResourceNotFoundException;
import com.resumeiq.resumeiq.repository.ResumeAnalysisRepository;
import com.resumeiq.resumeiq.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalysisService {
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final AtsScoringService atsScoringService;
    private final AiAnalysisProvider aiAnalysisProvider;

    public AnalysisService(ResumeRepository resumeRepository,
                           ResumeAnalysisRepository analysisRepository,
                           AtsScoringService atsScoringService,
                           AiAnalysisProvider aiAnalysisProvider) {
        this.resumeRepository = resumeRepository;
        this.analysisRepository = analysisRepository;
        this.atsScoringService = atsScoringService;
        this.aiAnalysisProvider = aiAnalysisProvider;
    }

    public ResumeAnalysis analyzeGeneral(Long resumeId, User user, String jobDescription) {
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        if (resume.getExtractedText() == null || resume.getExtractedText().isBlank()) {
            throw new BadRequestException("Resume does not contain extractable text");
        }

        AtsScoringService.AtsScoreResult score = atsScoringService.scoreResume(resume.getExtractedText(), jobDescription);
        AiAnalysisProvider.AnalysisResult ai = aiAnalysisProvider.analyze(resume.getExtractedText(), jobDescription);

        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setUser(user);
        analysis.setResume(resume);
        analysis.setJobDescription(jobDescription);
        analysis.setOverallScore((score.overall() + ai.overallScore()) / 2);
        analysis.setAtsScore(score.ats());
        analysis.setMatchedKeywords(String.join(", ", score.matchedKeywords()));
        analysis.setMissingKeywords(String.join(", ", score.missingKeywords()));
        analysis.setMatchedSkills(String.join(", ", score.matchedSkills()));
        analysis.setMissingSkills(String.join(", ", score.missingSkills()));
        analysis.setStrengths(ai.strengths());
        analysis.setWeaknesses(ai.weaknesses());
        analysis.setRecommendations(ai.recommendations());
        analysis.setSectionFeedback(ai.sectionFeedback());

        return analysisRepository.save(analysis);
    }

    public List<ResumeAnalysis> getAnalysesForUser(User user) {
        return analysisRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Long getLatestAnalysisIdForResume(Long resumeId, User user) {
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        return analysisRepository.findByResumeOrderByCreatedAtDesc(resume).stream()
                .findFirst().map(ResumeAnalysis::getId).orElse(null);
    }

    public ResumeAnalysis getAnalysis(Long id, User user) {
        return analysisRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
    }

    public AnalysisDtos.AnalysisResponse toDto(ResumeAnalysis analysis) {
        return new AnalysisDtos.AnalysisResponse(
                analysis.getId(),
                analysis.getResume().getId(),
                analysis.getJobDescription(),
                analysis.getOverallScore(),
                analysis.getAtsScore(),
                analysis.getMatchedKeywords(),
                analysis.getMissingKeywords(),
                analysis.getMatchedSkills(),
                analysis.getMissingSkills(),
                analysis.getStrengths(),
                analysis.getWeaknesses(),
                analysis.getRecommendations(),
                analysis.getSectionFeedback()
        );
    }
}
