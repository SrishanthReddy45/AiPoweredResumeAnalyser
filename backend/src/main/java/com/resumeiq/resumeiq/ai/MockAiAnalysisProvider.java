package com.resumeiq.resumeiq.ai;

import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiAnalysisProvider implements AiAnalysisProvider {
    @Override
    public AnalysisResult analyze(String resumeText, String jobDescription) {
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException("Resume text is empty; analysis cannot proceed");
        }
        String resume = resumeText.toLowerCase();
        String job = jobDescription == null ? "" : jobDescription.toLowerCase();
        java.util.List<String> skills = java.util.List.of("java", "spring boot", "spring", "react", "javascript", "typescript", "python", "sql", "mysql", "postgresql", "aws", "azure", "docker", "git", "rest api", "microservices", "junit", "kafka", "redis", "mongodb");
        java.util.List<String> matched = skills.stream().filter(skill -> resume.contains(skill) && (job.isBlank() || job.contains(skill))).distinct().toList();
        java.util.List<String> missing = skills.stream().filter(skill -> job.contains(skill) && !resume.contains(skill)).distinct().toList();
        int score = job.isBlank() ? Math.min(100, 45 + Math.min(30, resumeText.length() / 100) + (resume.contains("project") ? 10 : 0) + (resume.contains("education") ? 5 : 0))
                : (int) Math.round(job.isBlank() ? 0 : (matched.size() * 100.0 / Math.max(1, matched.size() + missing.size())));
        String strengths = (resume.contains("project") ? "Projects are present. " : "Consider adding a projects section if you have relevant work. ")
                + (resume.contains("experience") || resume.contains("internship") ? "Experience or internship information is present." : "Experience evidence was not clearly detected.");
        String weaknesses = (resume.contains("summary") ? "Review the summary for role-specific focus. " : "A professional summary heading was not detected. ")
                + (resume.contains("%") || resume.matches("(?s).*\\b\\d+\\+?\\s*(users|clients|percent|%|ms|seconds|hours).*" ) ? "Some measurable impact may be present; verify that all metrics are accurate." : "No obvious measurable impact metrics were detected.");
        String recommendations = missing.isEmpty() ? "Tailor the summary to the target role, use concise action-led bullets, and quantify outcomes only where you have verified figures."
                : "If you genuinely have these skills, make them visible with evidence in projects or experience: " + String.join(", ", missing) + ". Do not add skills you do not have.";
        String sectionFeedback = "Local heuristic mode is active; this is not an LLM-generated review. Detected skills are based on text matches. Review section headings, clarity, relevance, and factual accuracy manually.";
        return new AnalysisResult(score, strengths, weaknesses, recommendations, sectionFeedback,
                String.join(", ", matched), String.join(", ", missing), String.join(", ", matched), String.join(", ", missing));
    }
}
