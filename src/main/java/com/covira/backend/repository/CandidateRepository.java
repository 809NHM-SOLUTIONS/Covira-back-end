package com.covira.backend.repository;

import com.covira.backend.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /*
     * True if any candidate has submitted this interview.
     *
     * Candidates are matched by interview ID. Older candidates, created
     * before the ID was stored, have no ID and are matched by title.
     * Uses "has a submission time" rather than a status, so it stays
     * true after the employer shortlists or rejects the candidate.
     */
    @Query("""
            select count(c) > 0 from Candidate c
            where c.employer.id = :employerId
              and c.submittedAt is not null
              and (c.interviewId = :interviewId
                   or (c.interviewId is null and c.interview = :title))
            """)
    boolean hasSubmissions(
            @Param("employerId") Long employerId,
            @Param("interviewId") Long interviewId,
            @Param("title") String title
    );

    /*
     * All candidates belonging to one interview (same matching rule).
     */
    @Query("""
            select c from Candidate c
            where c.employer.id = :employerId
              and (c.interviewId = :interviewId
                   or (c.interviewId is null and c.interview = :title))
            """)
    List<Candidate> findAllForInterview(
            @Param("employerId") Long employerId,
            @Param("interviewId") Long interviewId,
            @Param("title") String title
    );
}