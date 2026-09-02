package com.covira.backend.service;

import com.covira.backend.dto.*;
import com.covira.backend.entity.Candidate;
import com.covira.backend.entity.Interview;
import com.covira.backend.repository.CandidateRepository;
import com.covira.backend.repository.InterviewRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int MAX_ACTIVITY_ITEMS = 10;
    private static final int MAX_RECENT_INTERVIEWS = 6;

    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;

    public DashboardService(
            InterviewRepository interviewRepository,
            CandidateRepository candidateRepository
    ) {
        this.interviewRepository = interviewRepository;
        this.candidateRepository = candidateRepository;
    }

    public DashboardSummaryDto getSummary(Long employerId) {

        List<Interview> interviews =
                interviewRepository.findAllByEmployerIdOrderByIdDesc(employerId);

        List<Candidate> candidates =
                candidateRepository.findAllByEmployerIdOrderByIdDesc(employerId);

        return new DashboardSummaryDto(
                buildStats(interviews, candidates),
                buildRecentActivity(interviews, candidates),
                buildRecentInterviews(interviews, candidates),
                buildCandidateOverview(candidates),
                buildActionsRequiringAttention(interviews, candidates)
        );
    }

    private DashboardStatsDto buildStats(
            List<Interview> interviews,
            List<Candidate> candidates
    ) {

        long completed = candidates.stream()
                .filter(c -> "Completed".equalsIgnoreCase(c.getStatus()))
                .count();

        long pending = candidates.stream()
                .filter(c -> "Pending".equalsIgnoreCase(c.getStatus()))
                .count();

        return new DashboardStatsDto(
                interviews.size(),
                candidates.size(),
                completed,
                pending
        );
    }

    
    private List<ActivityItemDto> buildRecentActivity(
            List<Interview> interviews,
            List<Candidate> candidates
    ) {

        List<ActivityItemDto> activity = new ArrayList<>();

        for (Interview interview : interviews) {

            if (interview.getCreatedAt() == null) {
                continue;
            }

            activity.add(new ActivityItemDto(
                    "INTERVIEW_CREATED",
                    "You created the interview \"" + interview.getTitle() + "\".",
                    interview.getCreatedAt(),
                    interview.getId()
            ));
        }

        for (Candidate candidate : candidates) {

            if (!"Completed".equalsIgnoreCase(candidate.getStatus())
                    || candidate.getSubmittedAt() == null) {
                continue;
            }

            Long relatedInterviewId = findInterviewIdByTitle(
                    interviews,
                    candidate.getInterview()
            );

            activity.add(new ActivityItemDto(
                    "CANDIDATE_COMPLETED",
                    candidate.getName() + " completed the \"" + candidate.getInterview() + "\" interview.",
                    candidate.getSubmittedAt(),
                    relatedInterviewId
            ));
        }

        return activity.stream()
                .sorted(Comparator.comparing(ActivityItemDto::getTimestamp).reversed())
                .limit(MAX_ACTIVITY_ITEMS)
                .collect(Collectors.toList());
    }

  
    private List<RecentInterviewDto> buildRecentInterviews(
            List<Interview> interviews,
            List<Candidate> candidates
    ) {

        return interviews.stream()
                .limit(MAX_RECENT_INTERVIEWS)
                .map(interview -> {

                    List<Candidate> matched = candidates.stream()
                            .filter(c -> interview.getTitle().equals(c.getInterview()))
                            .collect(Collectors.toList());

                    long completedCount = matched.stream()
                            .filter(c -> "Completed".equalsIgnoreCase(c.getStatus()))
                            .count();

                    return new RecentInterviewDto(
                            interview.getId(),
                            interview.getTitle(),
                            interview.getPosition(),
                            interview.getStatus(),
                            matched.size(),
                            interview.getQuestions().size(),
                            (int) completedCount,
                            interview.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    private CandidateOverviewDto buildCandidateOverview(List<Candidate> candidates) {

        long awaitingReview = candidates.stream()
                .filter(c -> "Pending".equalsIgnoreCase(c.getStatus()))
                .count();

        long completed = candidates.stream()
                .filter(c -> "Completed".equalsIgnoreCase(c.getStatus()))
                .count();

        
        long newCandidates = awaitingReview;

        return new CandidateOverviewDto(
                newCandidates,
                awaitingReview,
                completed
        );
    }

   
    private List<ActionItemDto> buildActionsRequiringAttention(
            List<Interview> interviews,
            List<Candidate> candidates
    ) {

        List<ActionItemDto> actions = new ArrayList<>();

        long awaitingReview = candidates.stream()
                .filter(c -> "Completed".equalsIgnoreCase(c.getStatus()))
                .count();
       

        if (awaitingReview > 0) {
            actions.add(new ActionItemDto(
                    "CANDIDATES_AWAITING_REVIEW",
                    "Candidates Awaiting Review",
                    awaitingReview + " candidate" + (awaitingReview == 1 ? " has" : "s have")
                            + " completed an interview and " + (awaitingReview == 1 ? "is" : "are")
                            + " ready for you to review.",
                    awaitingReview,
                    "CANDIDATE_LIST",
                    "warning"
            ));
        }

        long draftInterviews = interviews.stream()
                .filter(i -> "Draft".equalsIgnoreCase(i.getStatus()))
                .count();

        if (draftInterviews > 0) {
            actions.add(new ActionItemDto(
                    "INTERVIEWS_IN_DRAFT",
                    "Interviews Still in Draft",
                    draftInterviews + " interview" + (draftInterviews == 1 ? " is" : "s are")
                            + " still in draft and not yet visible to candidates.",
                    draftInterviews,
                    "INTERVIEW_LIST",
                    "info"
            ));
        }

        long interviewsWithoutQuestions = interviews.stream()
                .filter(i -> i.getQuestions() == null || i.getQuestions().isEmpty())
                .count();

        if (interviewsWithoutQuestions > 0) {
            actions.add(new ActionItemDto(
                    "INTERVIEWS_WITHOUT_QUESTIONS",
                    "Interviews Missing Questions",
                    interviewsWithoutQuestions + " interview" + (interviewsWithoutQuestions == 1 ? " has" : "s have")
                            + " no questions yet, so candidates can't complete " + (interviewsWithoutQuestions == 1 ? "it" : "them") + ".",
                    interviewsWithoutQuestions,
                    "INTERVIEW_LIST",
                    "warning"
            ));
        }

        return actions;
    }

    private Long findInterviewIdByTitle(List<Interview> interviews, String title) {

        if (title == null) {
            return null;
        }

        return interviews.stream()
                .filter(i -> title.equals(i.getTitle()))
                .map(Interview::getId)
                .findFirst()
                .orElse(null);
    }
}