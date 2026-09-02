package com.covira.backend.dto;

import java.util.List;

public class DashboardSummaryDto {

    private DashboardStatsDto stats;
    private List<ActivityItemDto> recentActivity;
    private List<RecentInterviewDto> recentInterviews;
    private CandidateOverviewDto candidateOverview;
    private List<ActionItemDto> actionsRequiringAttention;

    public DashboardSummaryDto(
            DashboardStatsDto stats,
            List<ActivityItemDto> recentActivity,
            List<RecentInterviewDto> recentInterviews,
            CandidateOverviewDto candidateOverview,
            List<ActionItemDto> actionsRequiringAttention
    ) {
        this.stats = stats;
        this.recentActivity = recentActivity;
        this.recentInterviews = recentInterviews;
        this.candidateOverview = candidateOverview;
        this.actionsRequiringAttention = actionsRequiringAttention;
    }

    public DashboardStatsDto getStats() {
        return stats;
    }

    public List<ActivityItemDto> getRecentActivity() {
        return recentActivity;
    }

    public List<RecentInterviewDto> getRecentInterviews() {
        return recentInterviews;
    }

    public CandidateOverviewDto getCandidateOverview() {
        return candidateOverview;
    }

    public List<ActionItemDto> getActionsRequiringAttention() {
        return actionsRequiringAttention;
    }
}