package com.resumeiq.resumeiq.repository;

import com.resumeiq.resumeiq.entity.GeneratedResume;
import com.resumeiq.resumeiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeneratedResumeRepository extends JpaRepository<GeneratedResume, Long> {
    List<GeneratedResume> findByUserOrderByCreatedAtDesc(User user);
    Optional<GeneratedResume> findByIdAndUser(Long id, User user);
}
