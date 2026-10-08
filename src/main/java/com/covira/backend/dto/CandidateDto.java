package com.covira.backend.dto;

import java.util.List;

public record CandidateDto(
        Long id,
        String name,
        String email,
        String position,
        String interview,
        String department,
        String location,
        String status,
        String submittedAt,
        String submittedAtIso,
        List<CandidateResponseDto> responses,
        Long interviewId
) {

    /*
     * Keeps any existing code that builds a CandidateDto without an
     * interview ID working. The ID is simply null in that case.
     */
    public CandidateDto(
            Long id,
            String name,
            String email,
            String position,
            String interview,
            String department,
            String location,
            String status,
            String submittedAt,
            String submittedAtIso,
            List<CandidateResponseDto> responses
    ) {
        this(
                id,
                name,
                email,
                position,
                interview,
                department,
                location,
                status,
                submittedAt,
                submittedAtIso,
                responses,
                null
        );
    }
}