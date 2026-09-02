package com.covira.backend.dto;

public class DashboardStatsDto {

    private long totalInterviews;
    private long totalCandidates;
    private long completedInterviews;
    private long pendingResponses;

    public DashboardStatsDto(
            long totalInterviews,
            long totalCandidates,
            long completedInterviews,
            long pendingResponses
    ) {
        this.totalInterviews = totalInterviews;
        this.totalCandidates = totalCandidates;
        this.completedInterviews = completedInterviews;
        this.pendingResponses = pendingResponses;
    }

    public long getTotalInterviews() {
        return totalInterviews;
    }

    public long getTotalCandidates() {
        return totalCandidates;
    }

    public long getCompletedInterviews() {
        return completedInterviews;
    }

    public long getPendingResponses() {
        return pendingResponses;
    }
}