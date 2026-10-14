package com.resumeiq.resumeiq.repository;

import com.resumeiq.resumeiq.entity.Resume;
import com.resumeiq.resumeiq.entity.ResumeAnalysis;
import com.resumeiq.resumeiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    List<ResumeAnalysis> findByUserOrderByCreatedAtDesc(User user);
    List<ResumeAnalysis> findByResumeOrderByCreatedAtDesc(Resume resume);
    Optional<ResumeAnalysis> findByIdAndUser(Long id, User user);
}
