package com.covira.backend.notification;


public final class NotificationType {

    private NotificationType() {
    }


    public static final String CANDIDATE_SUBMITTED = "CANDIDATE_SUBMITTED";


    public static final String CANDIDATE_VIDEO_SUBMITTED = "CANDIDATE_VIDEO_SUBMITTED";

    public static final String INTERVIEW_DEADLINE_APPROACHING = "INTERVIEW_DEADLINE_APPROACHING";

    public static final String INTERVIEW_EXPIRED = "INTERVIEW_EXPIRED";

    public static final String CANDIDATE_SHORTLISTED = "CANDIDATE_SHORTLISTED";

    public static final String CANDIDATE_REJECTED = "CANDIDATE_REJECTED";

    public static final String CANDIDATE_AWAITING_REVIEW = "CANDIDATE_AWAITING_REVIEW";

    public static final String INTERVIEW_PUBLISHED = "INTERVIEW_PUBLISHED";
}