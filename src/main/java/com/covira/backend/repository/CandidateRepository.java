package com.covira.backend.repository;

import com.covira.backend.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository
        extends JpaRepository<Candidate, Long> {

    List<Candidate> findAllByEmployerIdOrderByIdDesc(
            Long employerId
    );

    Optional<Candidate> findByIdAndEmployerId(
            Long id,
            Long employerId
    );
        boolean existsByInterviewAndStatus(String interview, String status);
}