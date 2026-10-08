package com.covira.backend.service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.covira.backend.dto.CandidateDto;
import com.covira.backend.dto.CandidateResponseDto;
import com.covira.backend.entity.Candidate;
import com.covira.backend.entity.CandidateResponse;
import com.covira.backend.entity.Interview;
import com.covira.backend.entity.Question;
import com.covira.backend.notification.NotificationType;
import com.covira.backend.repository.CandidateRepository;
import com.covira.backend.repository.CandidateResponseRepository;
import com.covira.backend.repository.InterviewRepository;
import com.covira.backend.repository.QuestionRepository;

@Service
public class CandidateService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    /*
     * Video file types candidates are allowed to upload.
     * Anything else is stored as .webm.
     */
    private static final List<String> ALLOWED_VIDEO_EXTENSIONS =
            List.of(".webm", ".mp4", ".mov");

    private static final java.time.ZoneId SERVER_ZONE =
            java.time.ZoneId.of("Africa/Johannesburg");

    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final CandidateResponseRepository candidateResponseRepository;
    private final QuestionRepository questionRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;


    public CandidateService(
            CandidateRepository candidateRepository,
            InterviewRepository interviewRepository,
            CandidateResponseRepository candidateResponseRepository,
            QuestionRepository questionRepository,
            NotificationService notificationService,
            EmailService emailService
    ) {
        this.candidateRepository = candidateRepository;
        this.interviewRepository = interviewRepository;
        this.candidateResponseRepository = candidateResponseRepository;
        this.questionRepository = questionRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }


    /*
     * ============================================================
     * UPLOAD VIDEO RESPONSE
     * ============================================================
     */

    @Transactional
    public CandidateDto uploadVideoResponse(
            Long candidateId,
            String interviewToken,
            Integer questionNumber,
            MultipartFile video
    ) {

        if (video == null || video.isEmpty()) {
            throw new IllegalArgumentException(
                    "Video file is required."
            );
        }

        if (questionNumber == null) {
            throw new IllegalArgumentException(
                    "Question number is required."
            );
        }

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found."
                                )
                        );

        Interview interview =
                interviewRepository
                        .findByInterviewToken(interviewToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Interview link is invalid or expired."
                                )
                        );


        /*
         * Make sure the candidate belongs to the
         * employer that owns this interview.
         */

        if (!candidate.getEmployer()
                .getId()
                .equals(interview.getEmployer().getId())) {

            throw new IllegalArgumentException(
                    "Candidate does not belong to this interview."
            );
        }


        /*
         * Make sure the candidate belongs to
         * this interview.
         */

        if (!belongsToInterview(candidate, interview)) {

            throw new IllegalArgumentException(
                    "Candidate does not belong to this interview."
            );
        }


        /*
         * Videos can only be uploaded while the interview is
         * still in progress. Once it has been submitted (or
         * reviewed) the responses are final.
         */

        if (!"Pending".equalsIgnoreCase(candidate.getStatus())) {
            throw new IllegalArgumentException(
                    "This interview has already been submitted."
            );
        }


        /*
         * Find the interview question.
         */

        List<Question> questions =
                questionRepository
                        .findAllByInterviewIdOrderByQuestionOrderAsc(
                                interview.getId()
                        );

        Question question =
                questions.stream()
                        .filter(q ->
                                q.getQuestionOrder()
                                        .equals(questionNumber)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Interview question not found."
                                )
                        );


        /*
         * Create upload directory.
         */

        try {

            Path uploadDirectory =
                    Paths.get(
                            "uploads",
                            "candidate-responses"
                    );

            Files.createDirectories(
                    uploadDirectory
            );


            /*
             * Determine file extension.
             *
             * Only known video extensions are accepted, so a
             * candidate cannot store e.g. an .html file that
             * would later be served from /uploads/.
             */

            String originalFilename =
                    video.getOriginalFilename();

            String extension = ".webm";

            if (originalFilename != null &&
                    originalFilename.contains(".")) {

                String ext =
                        originalFilename
                                .substring(
                                        originalFilename.lastIndexOf(".")
                                )
                                .toLowerCase();

                if (ALLOWED_VIDEO_EXTENSIONS.contains(ext)) {
                    extension = ext;
                }
            }


            /*
             * Generate unique filename.
             */

            String filename =
                    UUID.randomUUID()
                            .toString()
                            + extension;

            Path filePath =
                    uploadDirectory.resolve(filename);


            /*
             * Save video.
             */

            Files.copy(
                    video.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            /*
             * URL stored in PostgreSQL.
             */

            String videoUrl =
                    "/uploads/candidate-responses/"
                            + filename;


            /*
             * Find existing response or create one.
             */

            CandidateResponse response =
                    candidateResponseRepository
                            .findByCandidateIdAndQuestionNumber(
                                    candidateId,
                                    questionNumber
                            )
                            .orElseGet(
                                    CandidateResponse::new
                            );


            response.setCandidate(candidate);

            response.setQuestionNumber(
                    question.getQuestionOrder()
            );

            response.setQuestion(
                    question.getQuestionText()
            );

            response.setVideoUrl(
                    videoUrl
            );

            response.setSubmittedAt(
                    LocalDateTime.now()
            );


            /*
             * Save response.
             */

            candidateResponseRepository.save(
                    response
            );


            /*
             * Return updated candidate.
             */

            return toDto(candidate);

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "Failed to save video response."
            );
        }
    }


    /*
     * ============================================================
     * GET ALL CANDIDATES
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<CandidateDto> getCandidates(
            Long employerId
    ) {

        return candidateRepository
                .findAllByEmployerIdOrderByIdDesc(employerId)
                .stream()
                .map(this::toDto)
                .toList();
    }


    /*
     * ============================================================
     * GET ONE CANDIDATE
     * ============================================================
     */

    @Transactional(readOnly = true)
    public CandidateDto getCandidate(
            Long employerId,
            Long candidateId
    ) {

        Candidate candidate =
                candidateRepository
                        .findByIdAndEmployerId(
                                candidateId,
                                employerId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found."
                                )
                        );

        return toDto(candidate);
    }


    /*
     * ============================================================
     * CREATE PUBLIC CANDIDATE
     * ============================================================
     */

    @Transactional
    public CandidateDto createPublicCandidate(
            String interviewToken,
            String name,
            String email
    ) {

        if (name == null ||
                name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        if (email == null ||
                email.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Email address is required."
            );
        }


        Interview interview =
                interviewRepository
                        .findByInterviewToken(interviewToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Interview link is invalid or expired."
                                )
                        );

        if (interview.isDeadlinePassed()) {
            throw new IllegalStateException(Interview.CLOSED_MESSAGE);
        }

        /*
         * Check whether this candidate already
         * exists for this interview.
         */

        List<Candidate> existingCandidates =
                candidateRepository
                        .findAllByEmployerIdOrderByIdDesc(
                                interview.getEmployer().getId()
                        );

        Candidate existingCandidate =
                existingCandidates
                        .stream()
                        .filter(candidate ->
                                candidate.getEmail()
                                        .equalsIgnoreCase(
                                                email.trim()
                                        )
                                        &&
                                belongsToInterview(
                                        candidate,
                                        interview
                                )
                        )
                        .findFirst()
                        .orElse(null);


        if (existingCandidate != null) {

            /*
             * "Pending" means they registered but never finished
             * submitting - let them resume where they left off.
             *
             * Anything else (Completed, Shortlisted, Rejected, ...)
             * means they already completed this interview, so block
             * re-registration instead of silently starting them over.
             */

            if (!"Pending".equalsIgnoreCase(existingCandidate.getStatus())) {

                throw new IllegalArgumentException(
                        "You have already completed this interview. " +
                                "Multiple submissions are not permitted."
                );
            }

            // Older candidates were linked by title only - link them now.
            if (existingCandidate.getInterviewId() == null) {
                existingCandidate.setInterviewId(interview.getId());
                candidateRepository.save(existingCandidate);
            }

            return toDto(existingCandidate);
        }


        /*
         * Create candidate.
         */

        Candidate candidate =
                new Candidate();

        candidate.setEmployer(
                interview.getEmployer()
        );

        candidate.setName(
                name.trim()
        );

        candidate.setEmail(
                email.trim()
        );

        candidate.setPosition(
                interview.getPosition()
        );

        candidate.setInterview(
                interview.getTitle()
        );

        candidate.setInterviewId(
                interview.getId()
        );

        candidate.setDepartment(
                interview.getDepartment()
        );

        candidate.setLocation(
                interview.getLocation()
        );

        candidate.setStatus(
                "Pending"
        );

        candidate.setSubmittedAt(
                null
        );


        Candidate savedCandidate =
                candidateRepository.save(
                        candidate
                );

        return toDto(savedCandidate);
    }


    /*
     * ============================================================
     * SUBMIT INTERVIEW
     * ============================================================
     */

    @Transactional
    public CandidateDto submitInterview(
            Long candidateId,
            Map<String, Object> answers
    ) {

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found."
                                )
                        );


        /*
         * An interview can only be submitted once. Without this,
         * anyone could resubmit to overwrite answers, or flip a
         * Shortlisted / Rejected candidate back to "Completed".
         */

        if (!"Pending".equalsIgnoreCase(candidate.getStatus())) {
            throw new IllegalArgumentException(
                    "This interview has already been submitted."
            );
        }


        /*
         * Find interview.
         */

        Interview interview = findInterviewFor(candidate);


        /*
         * Get questions once.
         */

        List<Question> questions =
                interview != null
                        ? questionRepository
                            .findAllByInterviewIdOrderByQuestionOrderAsc(
                                    interview.getId()
                            )
                        : List.of();


        /*
         * Save candidate answers.
         */

        if (answers != null) {

            for (Map.Entry<String, Object> entry :
                    answers.entrySet()) {

                try {

                    int questionIndex =
                            Integer.parseInt(
                                    entry.getKey()
                            );

                    Object answerValue =
                            entry.getValue();

                    String answer =
                            answerValue == null
                                    ? ""
                                    : answerValue.toString();


                    Question question = null;

                    if (questionIndex >= 0 &&
                            questionIndex < questions.size()) {

                        question =
                                questions.get(
                                        questionIndex
                                );
                    }


                    int questionNumber =
                            question != null
                                    ? question.getQuestionOrder()
                                    : questionIndex + 1;


                    /*
                     * Find existing response.
                     *
                     * This prevents duplicate responses
                     * if the candidate uploads a video first
                     * and submits the interview afterwards.
                     */

                    CandidateResponse response =
                            candidateResponseRepository
                                    .findByCandidateIdAndQuestionNumber(
                                            candidateId,
                                            questionNumber
                                    )
                                    .orElseGet(
                                            CandidateResponse::new
                                    );


                    response.setCandidate(
                            candidate
                    );

                    response.setQuestionNumber(
                            questionNumber
                    );

                    response.setQuestion(
                            question != null
                                    ? question.getQuestionText()
                                    : "Question " +
                                      questionNumber
                    );


                    /*
                     * Save the actual candidate answer.
                     */

                    response.setAnswer(
                            answer
                    );


                    /*
                     * IMPORTANT:
                     *
                     * Do not clear videoUrl here.
                     *
                     * A video may already have been uploaded
                     * before the candidate submits the interview.
                     */

                    if (response.getVideoUrl() == null) {
                        response.setVideoUrl(null);
                    }


                    response.setSubmittedAt(
                            LocalDateTime.now()
                    );


                    candidateResponseRepository.save(
                            response
                    );

                } catch (NumberFormatException ignored) {

                    /*
                     * Ignore invalid question keys.
                     */

                }
            }
        }


        /*
         * Mark candidate as completed.
         */

        candidate.setStatus(
                "Completed"
        );

        candidate.setSubmittedAt(
                LocalDateTime.now()
        );

        candidateRepository.save(
                candidate
        );

        notificationService.createForCandidate(
                NotificationType.CANDIDATE_SUBMITTED,
                "New Candidate Submission",
                "A candidate has submitted their interview responses and is ready for review.",
                candidate,
                interview != null ? interview.getId() : null
        );

        /*
         * Email the employer (and their CC address, if set) that a
         * candidate has submitted. Respects the same notification
         * toggle as the in-app alert. Best-effort - a mail failure
         * shouldn't fail the candidate's submission.
         */

        try {

            var employer = candidate.getEmployer();

            if (Boolean.TRUE.equals(employer.getNotifyNewCandidateApplications())) {

                emailService.sendCandidateSubmittedAlertEmail(
                        employer.getEmail(),
                        employer.getCcEmail(),
                        candidate.getName(),
                        candidate.getPosition(),
                        candidate.getInterview()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        /*
         * Let the candidate know their submission was received.
         * Email delivery issues should not fail the submission
         * itself, so this is best-effort.
         */

        try {

            emailService.sendCandidateSubmissionConfirmationEmail(
                    candidate.getEmail(),
                    candidate.getName(),
                    candidate.getInterview(),
                    candidate.getPosition()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }

        return toDto(candidate);
    }


    /*
     * ============================================================
     * RECORD REVIEW DECISION (SHORTLIST / REJECT)
     * ============================================================
     */

    @Transactional
    public CandidateDto recordDecision(
            Long employerId,
            Long candidateId,
            String decision
    ) {

        if (decision == null || decision.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "A decision is required."
            );
        }

        String normalized =
                decision.trim().toUpperCase();

        if (!normalized.equals("SHORTLIST") &&
                !normalized.equals("REJECT")) {

            throw new IllegalArgumentException(
                    "Decision must be either SHORTLIST or REJECT."
            );
        }

        Candidate candidate =
                candidateRepository
                        .findByIdAndEmployerId(
                                candidateId,
                                employerId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found."
                                )
                        );

        if (candidate.getSubmittedAt() == null) {

            throw new IllegalArgumentException(
                    "This candidate hasn't submitted their interview yet."
            );
        }

        String newStatus =
                normalized.equals("SHORTLIST")
                        ? "Shortlisted"
                        : "Rejected";

        candidate.setStatus(newStatus);

        Candidate savedCandidate =
                candidateRepository.save(candidate);

        try {

            emailService.sendCandidateDecisionEmail(
                    savedCandidate.getEmail(),
                    savedCandidate.getName(),
                    savedCandidate.getInterview(),
                    savedCandidate.getPosition(),
                    newStatus.toUpperCase()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }

        notificationService.createForCandidate(
                normalized.equals("SHORTLIST")
                        ? NotificationType.CANDIDATE_SHORTLISTED
                        : NotificationType.CANDIDATE_REJECTED,
                normalized.equals("SHORTLIST")
                        ? "Candidate Shortlisted"
                        : "Candidate Rejected",
                savedCandidate.getName() +
                        (normalized.equals("SHORTLIST")
                                ? " has been shortlisted."
                                : " has been rejected."),
                savedCandidate,
                null
        );

        return toDto(savedCandidate);
    }


    /*
     * ============================================================
     * INTERVIEW LINKING HELPERS
     * ============================================================
     *
     * Candidates are linked to their interview by interview ID.
     * Candidates created before the ID existed only have the
     * interview title, so those fall back to matching by title.
     */

    private boolean belongsToInterview(
            Candidate candidate,
            Interview interview
    ) {

        if (candidate.getInterviewId() != null) {
            return candidate.getInterviewId()
                    .equals(interview.getId());
        }

        return candidate.getInterview() != null
                && candidate.getInterview()
                        .equals(interview.getTitle());
    }

    private Interview findInterviewFor(Candidate candidate) {

        Long employerId = candidate.getEmployer().getId();

        if (candidate.getInterviewId() != null) {
            return interviewRepository
                    .findByIdAndEmployerId(
                            candidate.getInterviewId(),
                            employerId
                    )
                    .orElse(null);
        }

        return interviewRepository
                .findByTitleAndEmployerId(
                        candidate.getInterview(),
                        employerId
                )
                .orElse(null);
    }


    /*
     * ============================================================
     * CONVERT CANDIDATE TO DTO
     * ============================================================
     */

    private CandidateDto toDto(
            Candidate candidate
    ) {

        List<CandidateResponseDto> responses =
                candidate.getResponses()
                        .stream()
                        .map(this::toResponseDto)
                        .toList();


        return new CandidateDto(
                candidate.getId(),
                candidate.getName(),
                candidate.getEmail(),
                candidate.getPosition(),
                candidate.getInterview(),
                candidate.getDepartment(),
                candidate.getLocation(),
                candidate.getStatus(),

                candidate.getSubmittedAt() == null
                        ? "-"
                        : DATE_FORMAT.format(
                                candidate.getSubmittedAt()
                        ),

                candidate.getSubmittedAt() == null
                        ? null
                        : candidate.getSubmittedAt()
                                .atZone(SERVER_ZONE)
                                .toInstant()
                                .toString(),

                responses,

                candidate.getInterviewId()
        );
    }


    /*
     * ============================================================
     * CONVERT RESPONSE TO DTO
     * ============================================================
     */

    private CandidateResponseDto toResponseDto(
            CandidateResponse response
    ) {

        return new CandidateResponseDto(
                response.getId(),
                response.getQuestionNumber(),
                response.getQuestion(),
                response.getAnswer(),
                response.getVideoUrl(),
                response.getDuration(),

                response.getSubmittedAt() == null
                        ? "-"
                        : DATE_TIME_FORMAT.format(
                                response.getSubmittedAt()
                        )
        );
    }
}