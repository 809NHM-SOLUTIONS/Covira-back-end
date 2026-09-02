package com.covira.backend.dto;

public class CandidateOverviewDto {

    private long newCandidates;
    private long awaitingReview;
    private long completed;

    public CandidateOverviewDto(
            long newCandidates,
            long awaitingReview,
            long completed
    ) {
        this.newCandidates = newCandidates;
        this.awaitingReview = awaitingReview;
        this.completed = completed;
    }

    public long getNewCandidates() {
        return newCandidates;
    }

    public long getAwaitingReview() {
        return awaitingReview;
    }

    public long getCompleted() {
        return completed;
    }
}