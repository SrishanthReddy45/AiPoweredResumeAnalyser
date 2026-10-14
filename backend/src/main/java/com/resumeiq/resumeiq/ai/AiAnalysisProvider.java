package com.resumeiq.resumeiq.ai;

public interface AiAnalysisProvider {
    AnalysisResult analyze(String resumeText, String jobDescription);

    default String improveResume(String resumeText, String jobDescription, String recommendations) {
        return resumeText;
    }

    record AnalysisResult(int overallScore,
                          String strengths,
                          String weaknesses,
                          String recommendations,
                          String sectionFeedback,
                          String matchedSkills,
                          String missingSkills,
                          String matchedKeywords,
                          String missingKeywords) {}
}
