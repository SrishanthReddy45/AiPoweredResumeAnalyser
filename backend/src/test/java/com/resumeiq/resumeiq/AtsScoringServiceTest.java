package com.resumeiq.resumeiq;

import com.resumeiq.resumeiq.service.AtsScoringService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtsScoringServiceTest {
    private final AtsScoringService service = new AtsScoringService();

    @Test
    void generalReviewReturnsBoundedStructureScoreWithoutJobDescription() {
        AtsScoringService.AtsScoreResult result = service.scoreResume(
                "Professional Summary\nSkills\nJava Spring Boot\nProjects\nEducation", "");
        assertTrue(result.overall() >= 0 && result.overall() <= 100);
        assertTrue(result.ats() >= 0 && result.ats() <= 100);
        assertTrue(result.summary().contains("no job description"));
        assertTrue(result.matchedSkills().isEmpty());
    }

    @Test
    void jobSpecificReviewFindsMatchedAndMissingSkills() {
        AtsScoringService.AtsScoreResult result = service.scoreResume(
                "Java Spring Boot developer with MySQL and REST API projects", 
                "Required skills: Java, Spring Boot, MySQL, React, AWS");
        assertTrue(result.matchedSkills().contains("java"));
        assertTrue(result.missingSkills().contains("react"));
        assertTrue(result.ats() >= 0 && result.ats() <= 100);
    }
}
