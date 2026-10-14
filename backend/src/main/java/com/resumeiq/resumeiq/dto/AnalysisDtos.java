package com.resumeiq.resumeiq.dto;

import jakarta.validation.constraints.Size;

public class AnalysisDtos {
    public record AnalyzeGeneralRequest(@Size(max = 12000, message = "Job description must be 12,000 characters or fewer") String jobDescription) {}

    public record AnalysisResponse(Long id, Long resumeId, String jobDescription, Integer overallScore, Integer atsScore,
                                  String matchedKeywords, String missingKeywords, String matchedSkills, String missingSkills,
                                  String strengths, String weaknesses, String recommendations, String sectionFeedback) {}
}
