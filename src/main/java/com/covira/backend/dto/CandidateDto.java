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
        List<CandidateResponseDto> responses
) {
}