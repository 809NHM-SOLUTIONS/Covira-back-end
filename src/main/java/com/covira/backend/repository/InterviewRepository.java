package com.covira.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.covira.backend.entity.Interview;

@Repository
public interface InterviewRepository
        extends JpaRepository<Interview, Long> {

    List<Interview> findAllByEmployerIdOrderByIdDesc(
            Long employerId
    );

    Optional<Interview> findByIdAndEmployerId(
            Long id,
            Long employerId
    );

    Optional<Interview> findByInterviewToken(
            String interviewToken
    );

    Optional<Interview> findByTitleAndEmployerId(
            String title,
            Long employerId
    );
        boolean existsByEmployerIdAndTitleIgnoreCase(Long employerId, String title);
}