package com.resumeiq.resumeiq.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AtsScoringService {
    public AtsScoreResult scoreResume(String resumeText, String jobDescription) {
        String resume = (resumeText == null ? "" : resumeText).toLowerCase();
        String job = (jobDescription == null ? "" : jobDescription).toLowerCase();

        Set<String> resumeWords = normalize(resume);
        Set<String> jobWords = normalize(job);

        if (job.isBlank()) {
            int sectionCount = 0;
            for (String section : List.of("summary", "skills", "experience", "education", "projects", "certifications")) {
                if (resume.contains(section)) sectionCount++;
            }
            int structure = Math.min(100, 40 + sectionCount * 10);
            int lengthScore = Math.min(100, Math.max(20, resumeText.length() / 30));
            int overall = (int) Math.round(structure * 0.6 + lengthScore * 0.4);
            return new AtsScoreResult(overall, structure, List.of(), List.of(), List.of(), List.of(),
                    "General resume review: no job description was supplied, so this score reflects detectable structure and text completeness, not job fit.");
        }

        Set<String> commonSkills = normalize("java spring boot react javascript mysql sql aws docker agile jira git html css typescript python api microservices kafka redis node express mongodb postgresql azure ci cd testing junit rest api backend frontend full stack");
        Set<String> requiredSkills = jobWords.stream().filter(commonSkills::contains).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> matched = new LinkedHashSet<>(requiredSkills.stream().filter(resumeWords::contains).toList());
        Set<String> missing = new LinkedHashSet<>(requiredSkills.stream().filter(skill -> !resumeWords.contains(skill)).toList());

        double coverage = requiredSkills.isEmpty() ? 0 : (matched.size() * 100.0) / requiredSkills.size();
        double relevantExperience = resume.contains("experience") || resume.contains("worked") || resume.contains("engineer") ? 80 : 60;
        double structure = resume.contains("summary") || resume.contains("experience") || resume.contains("skills") || resume.contains("education") ? 85 : 70;
        double score = Math.min(100.0, Math.max(0.0,
                (coverage * 0.35) + (relevantExperience * 0.25) + (structure * 0.15) + (Math.min(100, (matched.size() + 5) * 6) * 0.25)));

        int overall = (int) Math.round(score);
        int ats = (int) Math.round(Math.min(100, Math.max(0, coverage * 0.7 + structure * 0.3)));

        return new AtsScoreResult(
                overall,
                ats,
                matched.stream().toList(),
                missing.stream().toList(),
                matched.stream().toList(),
                missing.stream().toList(),
                "ATS scoring is a deterministic application model and is not intended to mirror any single employer ATS formula."
        );
    }

    private Set<String> normalize(String text) {
        return Arrays.stream(text.replaceAll("[^a-z0-9+#\\s]", " ")
                        .split("\\s+"))
                .map(String::trim)
                .filter(word -> !word.isBlank() && word.length() > 2)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public record AtsScoreResult(int overall, int ats, List<String> matchedSkills, List<String> missingSkills,
                                List<String> matchedKeywords, List<String> missingKeywords, String summary) {
    }
}
